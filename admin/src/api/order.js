import request from './request'

// 订单
export const listOrders = (params) => request.get('/admin/order/list', { params })
export const orderDetail = (id) => request.get(`/admin/order/${id}`)
export const cancelOrder = (id) => request.put(`/admin/order/${id}/cancel`)