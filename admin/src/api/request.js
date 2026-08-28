import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'
import { useUserStore } from '@/store/user'

const { message } = createDiscreteApi(['message'])

/**
 * 雪花 ID 超过 JS 安全整数范围（2^53-1），JSON.parse 会静默丢精度，
 * 回传后端时就是错的 ID（如删除报 "not found"）。
 * 递归把响应对象里所有 *Id / *Ids 字段转成字符串，保证前后端往返不损坏。
 */
export function stringifyIds(value) {
  if (Array.isArray(value)) {
    value.forEach(stringifyIds)
  } else if (value && typeof value === 'object') {
    Object.keys(value).forEach((k) => {
      const v = value[k]
      if (k.endsWith('Id') && typeof v === 'number') {
        value[k] = String(v)
      } else if (k.endsWith('Ids') && Array.isArray(v)) {
        value[k] = v.map((x) => (typeof x === 'number' ? String(x) : x))
      } else if (v && typeof v === 'object') {
        stringifyIds(v)
      }
    })
  }
  return value
}

const service = axios.create({ baseURL: '/api', timeout: 15000 })

service.interceptors.request.use((config) => {
  const user = useUserStore()
  if (user.token) config.headers.Authorization = user.token
  return config
})

service.interceptors.response.use(
  (res) => {
    const data = res.data
    // 统一返回 {code,message,data}
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 200) return stringifyIds(data.data)
      if (data.code === 401) {
        const user = useUserStore()
        user.logout()
        window.location.href = '/login'
      }
      // ===== 业务错误：打印完整信息便于定位 =====
      console.log('[API 业务错误]', res.config?.method?.toUpperCase(), res.config?.url,
        '| HTTP', res.status,
        '| 响应体:', JSON.stringify(data),
        '| 描述:', data.message)
      message.error(data.message || '操作失败')
      return Promise.reject(data)
    }
    return data
  },
  (err) => {
    // ===== 请求失败（HTTP 错误 / 网络错误）：打印完整信息便于定位 =====
    console.log('[API 请求失败]',
      err.config?.method?.toUpperCase(), err.config?.url,
      '| HTTP', err.response?.status,
      '| 响应体:', JSON.stringify(err.response?.data),
      '| 描述:', err.message,
      '| 超时:', err.code === 'ECONNABORTED',
      err)
    if (err.response?.status === 401) {
      const user = useUserStore()
      user.logout()
      window.location.href = '/login'
    }
    message.error(err.response?.data?.message || err.message || '请求失败')
    return Promise.reject(err)
  }
)

export default service