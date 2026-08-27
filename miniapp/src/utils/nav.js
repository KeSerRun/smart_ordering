// ========== 导航栏计算（跨端统一） ==========
// 微信有"胶囊"定位，支付宝/H5 无；统一防御式取值：
//   微信端取系统状态栏 + 胶囊定位；其他端退化为「状态栏高度 + 44px 标题栏」。
export function calcNavBar() {
  const windowInfo = typeof uni.getWindowInfo === 'function'
    ? uni.getWindowInfo()
    : (typeof uni.getSystemInfoSync === 'function' ? uni.getSystemInfoSync() : {});

  let menuBtn = null;
  // 仅微信小程序存在 getMenuButtonBoundingClientRect，其他端整块被条件编译移除
  // #ifdef MP-WEIXIN
  if (typeof uni.getMenuButtonBoundingClientRect === 'function') {
    menuBtn = uni.getMenuButtonBoundingClientRect();
  }
  // #endif

  const statusBarHeight = Number(windowInfo.statusBarHeight) || 0;
  let navBarHeight = 44;
  if (menuBtn && menuBtn.top) {
    navBarHeight = (menuBtn.top - statusBarHeight) * 2 + menuBtn.height;
  }
  return { statusBarHeight, navBarHeight };
}