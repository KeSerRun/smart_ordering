// ========== 防抖工具 ==========
// 原样迁移自 miniapp/utils/debounce.js
export function debounce(fn, wait) {
  let timer = null;
  return function debounced(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), wait);
  };
}