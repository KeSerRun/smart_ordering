const tableApi = require('../api/table');
const { isLoggedIn } = require('./auth');
const { KEYS, get, setCurrentTable } = require('./storage');

function normalizeTableCode(value) {
  if (value === null || value === undefined) return '';
  return String(value).trim().toUpperCase();
}

function assertTableAvailable(status) {
  if (status === 0 || status === 1) return;
  if (status === 2 || status === 3) {
    throw new Error('当前桌台正在收尾，请联系服务员处理');
  }
  throw new Error('当前桌台不可用');
}

async function reloadTable(code, fallbackTable) {
  const latest = await tableApi.getTableByCode(fallbackTable.code || code);
  return { ...fallbackTable, ...latest };
}

async function openAndReloadTable(code, table) {
  await tableApi.openTable(table.id);
  const openedTable = await reloadTable(code, table);
  return { ...openedTable, status: 1 };
}

async function ensureCurrentUserTableBinding(tableOrId) {
  if (!isLoggedIn()) {
    return typeof tableOrId === 'object' ? tableOrId : null;
  }

  const tableId = typeof tableOrId === 'object'
    ? Number((tableOrId || {}).id || 0)
    : Number(tableOrId || 0);
  if (!tableId) {
    return typeof tableOrId === 'object' ? tableOrId : null;
  }

  const boundTable = await tableApi.bindCurrentUser(tableId);
  setCurrentTable(boundTable);
  return boundTable;
}

async function previewTableByCode(tableCode) {
  if (!tableCode) {
    throw new Error('桌号编码不能为空');
  }

  const targetTable = await tableApi.getTableByCode(tableCode);
  assertTableAvailable(Number(targetTable.status));
  return targetTable;
}

async function bindTableByCode(tableCode, options = {}) {
  if (!tableCode) {
    throw new Error('桌号编码不能为空');
  }

  const currentTable = options.currentTable === undefined ? get(KEYS.TABLE) : options.currentTable;
  const targetTable = await tableApi.getTableByCode(tableCode);
  const targetStatus = Number(targetTable.status);
  const currentId = currentTable && currentTable.id ? Number(currentTable.id) : 0;
  const targetId = targetTable && targetTable.id ? Number(targetTable.id) : 0;
  let boundTable = { ...targetTable };
  let entryMode = 'resume';

  assertTableAvailable(targetStatus);

  if (targetStatus === 0) {
    entryMode = 'open';
  } else if (currentId && targetId && currentId !== targetId) {
    entryMode = 'join';
  }

  if (isLoggedIn()) {
    boundTable = await ensureCurrentUserTableBinding(targetTable);
  } else if (targetStatus === 0) {
    boundTable = await openAndReloadTable(tableCode, targetTable);
  }

  setCurrentTable(boundTable);
  return {
    table: boundTable,
    entryMode,
    originalStatus: targetStatus
  };
}

module.exports = {
  bindTableByCode,
  previewTableByCode,
  ensureCurrentUserTableBinding,
  normalizeTableCode
};
