const orderApi = require('../../api/order');
const paymentApi = require('../../api/payment');
const { KEYS, get, set } = require('../../utils/storage');

function normalizeOrderId(v) {
  if (v === null || v === undefined) return '';
  const s = String(v).trim();
  return s && s !== '0' ? s : '';
}

function amountToFen(value) {
  return Math.round(Number(value || 0) * 100);
}

Page({
  data: {
    orderId: '',
    order: null,
    paying: false,
    paymentStatusText: ''
  },

  onLoad(query) {
    const orderId = normalizeOrderId(query.orderId || get(KEYS.ORDER_ID));
    this.setData({ orderId });
    if (!orderId) {
      wx.showToast({ title: '未找到订单号，请重新下单', icon: 'none' });
      return;
    }
    set(KEYS.ORDER_ID, orderId);
    this.loadOrder(orderId);
  },

  async loadOrder(id) {
    try {
      const order = await orderApi.getOrder(id);
      this.setData({ order });
    } catch (err) {
      wx.showToast({ title: err.message || '获取订单失败', icon: 'none' });
    }
  },

  async payNow() {
    if (this.data.paying) return;

    const orderId = normalizeOrderId(this.data.orderId || get(KEYS.ORDER_ID));
    if (!orderId) {
      wx.showToast({ title: '订单号丢失，请返回重试', icon: 'none' });
      return;
    }

    const amount = Number((this.data.order && this.data.order.actualAmount) || 0);
    if (!(amount > 0)) {
      wx.showToast({ title: '订单金额无效', icon: 'none' });
      return;
    }

    if (this.data.order && Number(this.data.order.status) === 1) {
      wx.showToast({ title: '该订单已支付', icon: 'none' });
      wx.navigateTo({ url: `/pages/result/index?ok=1&orderId=${orderId}` });
      return;
    }

    this.setData({ paying: true, paymentStatusText: '支付处理中...' });

    try {
      const payData = await paymentApi.wechatPay(orderId);
      // 开发环境 mock 支付：后端已模拟支付成功，直接跳成功页，不拉起微信支付
      if (payData && payData.mockPaid) {
        await this.loadOrder(orderId);
        this.setData({ paymentStatusText: '支付成功' });
        wx.navigateTo({ url: `/pages/result/index?ok=1&orderId=${orderId}` });
        return;
      }
      if (payData && payData.amount !== undefined && amountToFen(payData.amount) !== amountToFen(amount)) {
        await this.loadOrder(orderId);
        throw new Error('支付金额已更新，请重新确认后再支付');
      }
      await this.requestWechatPayment(payData);
      await this.loadOrder(orderId);
      this.setData({ paymentStatusText: '支付成功' });
      wx.navigateTo({ url: `/pages/result/index?ok=1&orderId=${orderId}` });
    } catch (err) {
      wx.showToast({ title: err.message || '支付失败', icon: 'none' });
    } finally {
      this.setData({ paying: false });
    }
  },

  requestWechatPayment(payData) {
    if (!payData || !payData.timeStamp || !payData.nonceStr || !payData.packageValue || !payData.paySign) {
      return Promise.reject(new Error('后端未返回完整的微信支付参数'));
    }

    return new Promise((resolve, reject) => {
      wx.requestPayment({
        timeStamp: String(payData.timeStamp),
        nonceStr: payData.nonceStr,
        package: payData.packageValue,
        signType: payData.signType || 'RSA',
        paySign: payData.paySign,
        success: resolve,
        fail: (err) => {
          console.error('微信支付拉起失败', {
            errMsg: err && err.errMsg,
            errno: err && err.errno,
            errCode: err && err.errCode,
            raw: err,
            payParams: {
              appId: payData.appId,
              timeStamp: String(payData.timeStamp),
              nonceStr: payData.nonceStr,
              packageValue: payData.packageValue,
              signType: payData.signType || 'RSA',
              paySignLength: payData.paySign ? String(payData.paySign).length : 0
            }
          });
          if (err && err.errMsg && err.errMsg.includes('cancel')) {
            reject(new Error('已取消支付'));
            return;
          }
          reject(new Error((err && err.errMsg) || '微信支付失败'));
        }
      });
    });
  },

  goReview() {
    wx.navigateTo({ url: `/pages/review/index?orderId=${this.data.orderId}` });
  }
});
