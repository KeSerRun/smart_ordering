const KEYS = {
  TOKEN: 'appToken',
  OPENID: 'appOpenid',
  USER_INFO: 'appUserInfo',
  TABLE: 'currentTable',
  ORDER_ID: 'currentOrderId',
  ORDERED_DISH_IDS: 'orderedDishIds',
  MOCK_PAID_ORDER_IDS: 'mockPaidOrderIds',
  REVIEWED_ORDER_IDS: 'reviewedOrderIds',
  PERSON_COUNT: 'personCount'
};

function get(key) {
  return wx.getStorageSync(key);
}

function set(key, value) {
  wx.setStorageSync(key, value);
}

function remove(key) {
  wx.removeStorageSync(key);
}

function normalizeTableIdentity(table) {
  if (!table || typeof table !== 'object') return '';
  const id = table.id === null || table.id === undefined ? '' : String(table.id).trim();
  const sessionCode = table.currentSessionCode === null || table.currentSessionCode === undefined
    ? ''
    : String(table.currentSessionCode).trim();
  if (id) return sessionCode ? `${id}#${sessionCode}` : id;
  const code = table.code === null || table.code === undefined ? '' : String(table.code).trim();
  return sessionCode ? `${code}#${sessionCode}` : code;
}

function getTableBindingKey(table) {
  return normalizeTableIdentity(table);
}

function clearCurrentTableState() {
  remove(KEYS.ORDER_ID);
  remove(KEYS.ORDERED_DISH_IDS);
  remove(KEYS.PERSON_COUNT);
}

function setCurrentTable(table) {
  if (!table) {
    remove(KEYS.TABLE);
    clearCurrentTableState();
    return;
  }

  const currentTable = get(KEYS.TABLE);
  const currentIdentity = normalizeTableIdentity(currentTable);
  const nextIdentity = normalizeTableIdentity(table);
  if (currentIdentity && nextIdentity && currentIdentity !== nextIdentity) {
    clearCurrentTableState();
  }

  set(KEYS.TABLE, table);
}

module.exports = {
  KEYS,
  get,
  set,
  remove,
  clearCurrentTableState,
  setCurrentTable,
  getTableBindingKey
};
