const cartApi = require('../../api/cart');
const couponApi = require('../../api/coupon');
const memberApi = require('../../api/member');
const orderApi = require('../../api/order');
const { formatPrice } = require('../../utils/format');
const { KEYS, get, set, getTableBindingKey } = require('../../utils/storage');

const WEEKDAY_LABELS = {
  1: '周一',
  2: '周二',
  3: '周三',
  4: '周四',
  5: '周五',
  6: '周六',
  7: '周日'
};

function getCurrentWeekday() {
  const weekday = new Date().getDay();
  return weekday === 0 ? 7 : weekday;
}

function formatAvailableWeekdays(availableWeekdays) {
  if (!availableWeekdays) return '全周可用';
  const labels = String(availableWeekdays)
    .split(',')
    .map(item => WEEKDAY_LABELS[Number(item)])
    .filter(Boolean);
  return labels.length ? labels.join('、') : '全周可用';
}

function isCouponAvailableToday(coupon) {
  if (!coupon || !coupon.availableWeekdays) return true;
  const currentWeekday = getCurrentWeekday();
  return String(coupon.availableWeekdays)
    .split(',')
    .map(item => Number(item))
    .includes(currentWeekday);
}

function pickOrderId(order) {
  if (!order || typeof order !== 'object') return '';
  const raw = order.id ?? order.orderId ?? '';
  return raw === null || raw === undefined ? '' : String(raw);
}

function pickLatestPendingOrder(orders) {
  if (!Array.isArray(orders) || orders.length === 0) return null;
  return orders.find(order => Number(order.status) === 0) || null;
}

function normalizeDishId(value) {
  if (value === null || value === undefined) return '';
  return String(value);
}

Page({
  data: {
    tableId: null,
    tableBindingKey: '',
    availableCoupons: [],
    selectedCouponId: null,
    selectedCouponText: '暂不使用优惠券',
    memberCenter: null,
    benefitOverview: null,
    requestedPoints: 0,
    actualUsedPoints: 0,
    pointsDeductionAmount: '0.00',
    pointsText: '暂不使用积分',
    payableTotal: '0.00',
    cart: {
      items: [],
      totalCount: 0,
      totalPrice: '0.00'
    }
  },

  onLoad(query) {
    const table = get(KEYS.TABLE) || {};
    const tableId = Number(query.tableId || table.id);
    this.setData({ tableId, tableBindingKey: getTableBindingKey(table) });
  },

  onShow() {
    const table = get(KEYS.TABLE) || {};
    const tableId = Number(table.id || 0);
    const tableBindingKey = getTableBindingKey(table);
    if (tableBindingKey && tableBindingKey !== this.data.tableBindingKey) {
      this.setData({
        tableId,
        tableBindingKey,
        selectedCouponId: null,
        selectedCouponText: '暂不使用优惠券',
        requestedPoints: 0,
        actualUsedPoints: 0,
        pointsDeductionAmount: '0.00',
        pointsText: '暂不使用积分'
      });
    } else if (tableId && tableId !== Number(this.data.tableId || 0)) {
      this.setData({ tableId, tableBindingKey });
    }
    this.loadCart();
    this.loadCoupons();
    this.loadMemberBenefits();
  },

  async loadCart() {
    if (!this.data.tableId) return;
    try {
      const cart = await cartApi.getCart(this.data.tableId);
      this.setData({
        cart: {
          ...cart,
          totalPrice: formatPrice(cart.totalPrice),
          items: cart.items || []
        }
      }, () => this.syncPointsDeduction());
    } catch (err) {
      this.setData({ cart: { items: [], totalCount: 0, totalPrice: '0.00' }, payableTotal: '0.00' });
    }
  },

  async loadMemberBenefits() {
    try {
      const [memberCenter, benefitOverview] = await Promise.all([
        memberApi.getMemberCenter(),
        memberApi.getMemberBenefitOverview()
      ]);
      this.setData({ memberCenter, benefitOverview }, () => this.syncPointsDeduction());
    } catch (err) {
      this.setData({ memberCenter: null, benefitOverview: null, requestedPoints: 0, actualUsedPoints: 0, pointsDeductionAmount: '0.00' });
    }
  },

  async loadCoupons() {
    try {
      const result = await couponApi.getMyCoupons({ status: 0, pageNum: 1, pageSize: 100 });
      this.setData({ availableCoupons: result.list || [] });
      this.syncSelectedCoupon();
    } catch (err) {
      this.setData({ availableCoupons: [] });
    }
  },

  async increase(e) {
    const item = e.currentTarget.dataset.item;
    await this.updateQuantity(item, Number(item.quantity || 0) + 1);
  },

  async decrease(e) {
    const item = e.currentTarget.dataset.item;
    await this.updateQuantity(item, Number(item.quantity || 0) - 1);
  },

  async updateQuantity(item, quantity) {
    try {
      if (quantity <= 0) {
        await cartApi.removeCartItem(item.dishId, this.data.tableId);
      } else {
        await cartApi.updateCartItem(item.dishId, this.data.tableId, quantity);
      }
      this.loadCart();
    } catch (err) {
      wx.showToast({ title: err.message || '更新失败', icon: 'none' });
    }
  },

  onRemarkInput(e) {
    const dishId = normalizeDishId(e.currentTarget.dataset.id);
    const value = e.detail.value;
    const items = this.data.cart.items.map((it) => (
      normalizeDishId(it.dishId) === dishId ? { ...it, remark: value } : it
    ));
    this.setData({ 'cart.items': items });
  },

  async saveRemark(e) {
    const dishId = normalizeDishId(e.currentTarget.dataset.id);
    const item = this.data.cart.items.find(it => normalizeDishId(it.dishId) === dishId);
    if (!item) {
      wx.showToast({ title: '未找到当前菜品', icon: 'none' });
      return;
    }
    try {
      await cartApi.updateCartItem(item.dishId, this.data.tableId, undefined, item.remark ?? '');
      await this.loadCart();
      wx.showToast({ title: '备注已保存', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: err.message || '备注保存失败', icon: 'none' });
    }
  },

  async clearCart() {
    try {
      await cartApi.clearCart(this.data.tableId);
      this.loadCart();
      wx.showToast({ title: '已清空', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: err.message || '清空失败', icon: 'none' });
    }
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/index' });
  },

  syncSelectedCoupon() {
    const selectedCoupon = this.data.availableCoupons.find(item => Number(item.id) === Number(this.data.selectedCouponId || 0));
    if (!selectedCoupon) {
      this.setData({ selectedCouponId: null, selectedCouponText: '暂不使用优惠券' });
      return;
    }

    const thresholdAmount = Number(selectedCoupon.thresholdAmount || 0);
    const cartTotal = Number(this.data.cart.totalPrice || 0);
    const ruleText = selectedCoupon.couponType === 1
      ? `满${selectedCoupon.thresholdAmount}减${selectedCoupon.discountAmount}`
      : `${selectedCoupon.discountRate}折优惠`;

    const weekdayText = formatAvailableWeekdays(selectedCoupon.availableWeekdays);

    if (selectedCoupon.couponType === 1 && cartTotal < thresholdAmount) {
      this.setData({
        selectedCouponText: `${ruleText}（${weekdayText}，当前金额未达门槛）`
      });
      return;
    }

    if (!isCouponAvailableToday(selectedCoupon)) {
      this.setData({
        selectedCouponText: `${ruleText}（${weekdayText}）`
      });
      return;
    }

    this.setData({
      selectedCouponText: `${ruleText}（${weekdayText}）`
    });
  },

  syncPointsDeduction() {
    const rule = this.data.benefitOverview?.pointsDeductionRule;
    const pointsBalance = Number(this.data.memberCenter?.pointsBalance || 0);
    const cartTotal = Number(this.data.cart.totalPrice || 0);
    if (!rule || !rule.enabled || cartTotal <= 0 || pointsBalance <= 0) {
      this.setData({ requestedPoints: 0, actualUsedPoints: 0, pointsDeductionAmount: '0.00', pointsText: '暂不使用积分', payableTotal: formatPrice(cartTotal) });
      return;
    }
    const pointsPerStep = Number(rule.pointsPerStep || 0);
    const amountPerStep = Number(rule.amountPerStep || 0);
    const maxRatio = Number(rule.maxDeductionRatio || 0);
    const maxPointsPerOrder = Number(rule.maxPointsPerOrder || 0);
    if (pointsPerStep <= 0 || amountPerStep <= 0) {
      this.setData({ requestedPoints: 0, actualUsedPoints: 0, pointsDeductionAmount: '0.00', pointsText: '暂不使用积分', payableTotal: formatPrice(cartTotal) });
      return;
    }

    const maxDeductionAmount = Math.floor(cartTotal * maxRatio * 100) / 100;
    const maxByAmount = Math.floor(maxDeductionAmount / amountPerStep) * pointsPerStep;
    let maxPoints = Math.min(pointsBalance, maxByAmount);
    if (maxPointsPerOrder > 0) {
      maxPoints = Math.min(maxPoints, maxPointsPerOrder);
    }
    const normalizedMax = Math.floor(maxPoints / pointsPerStep) * pointsPerStep;
    const requested = Math.min(Number(this.data.requestedPoints || 0), normalizedMax);
    const actualUsedPoints = Math.floor(requested / pointsPerStep) * pointsPerStep;
    const deductionAmount = ((actualUsedPoints / pointsPerStep) * amountPerStep).toFixed(2);
    const payableTotal = formatPrice(Math.max(cartTotal - Number(deductionAmount || 0), 0));
    this.setData({
      actualUsedPoints,
      pointsDeductionAmount: deductionAmount,
      pointsText: actualUsedPoints > 0 ? `${actualUsedPoints} 积分抵 ¥${deductionAmount}` : '暂不使用积分',
      payableTotal
    });
  },

  choosePoints() {
    const rule = this.data.benefitOverview?.pointsDeductionRule;
    const pointsBalance = Number(this.data.memberCenter?.pointsBalance || 0);
    const cartTotal = Number(this.data.cart.totalPrice || 0);
    if (!rule || !rule.enabled || cartTotal <= 0 || pointsBalance <= 0) {
      wx.showToast({ title: '当前不可用积分抵现', icon: 'none' });
      return;
    }

    const pointsPerStep = Number(rule.pointsPerStep || 0);
    const amountPerStep = Number(rule.amountPerStep || 0);
    const maxRatio = Number(rule.maxDeductionRatio || 0);
    const maxPointsPerOrder = Number(rule.maxPointsPerOrder || 0);
    const maxDeductionAmount = Math.floor(cartTotal * maxRatio * 100) / 100;
    const maxByAmount = Math.floor(maxDeductionAmount / amountPerStep) * pointsPerStep;
    let maxPoints = Math.min(pointsBalance, maxByAmount);
    if (maxPointsPerOrder > 0) maxPoints = Math.min(maxPoints, maxPointsPerOrder);
    const normalizedMax = Math.floor(maxPoints / pointsPerStep) * pointsPerStep;

    if (normalizedMax <= 0) {
      wx.showToast({ title: '本单暂不可使用积分', icon: 'none' });
      return;
    }

    const options = ['不使用积分'];
    for (let points = pointsPerStep; points <= normalizedMax; points += pointsPerStep) {
      const amount = ((points / pointsPerStep) * amountPerStep).toFixed(2);
      options.push(`${points} 积分抵 ${amount} 元`);
    }

    wx.showActionSheet({
      itemList: options,
      success: res => {
        if (res.tapIndex === 0) {
          this.setData({ requestedPoints: 0 }, () => this.syncPointsDeduction());
          return;
        }
        const selectedPoints = res.tapIndex * pointsPerStep;
        this.setData({ requestedPoints: selectedPoints }, () => this.syncPointsDeduction());
      }
    });
  },

  chooseCoupon() {
    const options = [{ label: '不使用优惠券', value: 0 }];
    this.data.availableCoupons.forEach(item => {
      const ruleLabel = item.couponType === 1
        ? `${item.couponName}｜满${item.thresholdAmount}减${item.discountAmount}`
        : `${item.couponName}｜${item.discountRate}折优惠`;
      const weekdayLabel = formatAvailableWeekdays(item.availableWeekdays);
      const disabledTip = isCouponAvailableToday(item) ? '' : '｜今日不可用';
      options.push({ label: `${ruleLabel}｜${weekdayLabel}${disabledTip}`, value: item.id });
    });

    wx.showActionSheet({
      itemList: options.map(item => item.label),
      success: (res) => {
        const selected = options[res.tapIndex];
        if (!selected || selected.value === 0) {
          this.setData({ selectedCouponId: null, selectedCouponText: '暂不使用优惠券' });
          return;
        }
        const selectedCoupon = this.data.availableCoupons.find(item => Number(item.id) === Number(selected.value));
        if (selectedCoupon && !isCouponAvailableToday(selectedCoupon)) {
          wx.showToast({ title: `${formatAvailableWeekdays(selectedCoupon.availableWeekdays)}可用`, icon: 'none' });
          return;
        }
        this.setData({ selectedCouponId: selected.value }, () => {
          this.syncSelectedCoupon();
        });
      }
    });
  },

  async submitOrder() {
    if (!this.data.cart.items.length) {
      wx.showToast({ title: '购物车为空', icon: 'none' });
      return;
    }

    try {
      const tableOrders = await orderApi.getTableOrders(this.data.tableId);
      const pendingOrder = pickLatestPendingOrder(tableOrders);

      if (pendingOrder) {
        if (this.data.selectedCouponId || Number(this.data.actualUsedPoints || 0) > 0) {
          wx.showToast({ title: '当前为加菜并单，暂不支持重新选择优惠券或积分', icon: 'none' });
          return;
        }

        const pendingOrderId = pickOrderId(pendingOrder);
        if (!pendingOrderId) {
          wx.showToast({ title: '当前活动订单异常，请稍后重试', icon: 'none' });
          return;
        }

        for (const item of this.data.cart.items) {
          await orderApi.addOrderItem(pendingOrderId, {
            dishId: item.dishId,
            quantity: item.quantity,
            remark: item.remark || ''
          });
        }

        await cartApi.clearCart(this.data.tableId);
        wx.showToast({ title: '加菜成功', icon: 'none' });
        wx.switchTab({ url: '/pages/order/index' });
        return;
      }

      const order = await orderApi.createOrder({
        tableId: this.data.tableId,
        paymentMode: 0,
        orderType: 0,
        remark: '',
        couponId: this.data.selectedCouponId || undefined,
        usePoints: this.data.actualUsedPoints || undefined
      });

      const orderId = pickOrderId(order);
      if (!orderId) {
        wx.showToast({ title: '下单成功但订单号缺失', icon: 'none' });
        return;
      }

      set(KEYS.ORDER_ID, orderId);
      wx.showToast({ title: '下单成功', icon: 'none' });
      wx.navigateTo({ url: `/pages/payment/index?orderId=${orderId}` });
    } catch (err) {
      wx.showToast({ title: err.message || '下单失败', icon: 'none' });
    }
  }
});
