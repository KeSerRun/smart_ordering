/**
 * 通用格式化工具
 */

const pad = (n) => String(n).padStart(2, '0')

/**
 * 时间 → 'YYYY-MM-DD HH:mm:ss'(本地时区)
 * 兼容:ms 时间戳、Date、后端 ISO 字符串('2026-08-28T10:06:00')
 */
export function formatDateTime(value) {
  if (value === null || value === undefined || value === '') return ''
  let d = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(d.getTime())) {
    // ISO 字符串在某些环境解析失败时,直接替换 T 截断
    const s = String(value)
    const m = s.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2}:\d{2})/)
    return m ? `${m[1]} ${m[2]}` : ''
  }
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/**
 * n-date-picker(type="daterange") 的值是 [起始日00:00, 结束日00:00] 的 ms 时间戳。
 * 转成后端查询区间:开始取所选起始日 00:00:00,结束取所选结束日 23:59:59(便于按天整查)。
 * 返回 { startTime, endTime },未选时均为 undefined。
 */
export function toTimeRange(range) {
  if (!Array.isArray(range) || !range[0] || !range[1]) return { startTime: undefined, endTime: undefined }
  return {
    startTime: formatDateTime(range[0]),
    endTime: formatDateTime(range[1] + 24 * 3600 * 1000 - 1)
  }
}