import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'
import { useUserStore } from '@/store/user'

const { message } = createDiscreteApi(['message'])

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
      if (data.code === 200) return data.data
      if (data.code === 401) {
        const user = useUserStore()
        user.logout()
        window.location.href = '/login'
      }
      message.error(data.message || '操作失败')
      return Promise.reject(data)
    }
    return data
  },
  (err) => {
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