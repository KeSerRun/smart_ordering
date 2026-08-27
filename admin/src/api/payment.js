import request from './request'

export const listPayments = (params) => request.get('/admin/payment/list', { params })
export const cashPay = (d) => request.post('/admin/payment/cash', d)