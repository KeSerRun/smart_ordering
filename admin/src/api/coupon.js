import request from './request'

export const listCouponTemplates = (params) => request.get('/admin/coupon/template/page', { params })
export const createCouponTemplate = (d) => request.post('/admin/coupon/template', d)
export const updateCouponTemplate = (id, d) => request.put(`/admin/coupon/template/${id}`, d)
export const updateCouponTemplateStatus = (id, status) => request.put(`/admin/coupon/template/${id}/status`, null, { params: { status } })
export const listUserCoupons = (params) => request.get('/admin/coupon/user/page', { params })