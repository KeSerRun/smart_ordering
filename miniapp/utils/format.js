function formatPrice(v) {
  const n = Number(v || 0);
  return n.toFixed(2);
}

function statusText(status) {
  const map = {
    0: '待制作',
    1: '制作中',
    2: '已完成'
  };
  return map[status] || '未知状态';
}

module.exports = {
  formatPrice,
  statusText
};