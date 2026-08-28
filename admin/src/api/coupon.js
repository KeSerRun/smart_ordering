import request from './request'

export const listCouponTemplates = (params) => request.get('/admin/coupon/template/page', { params })
export const createCouponTemplate = (d) => request.post('/admin/coupon/template', d)
export const updateCouponTemplate = (id, d) => request.put(`/admin/coupon/template/${id}`, d)
export const updateCouponTemplateStatus = (id, status) => request.put(`/admin/coupon/template/${id}/status`, null, { params: { status } })

// ===== 发券任务（MQ 异步，支持按会员等级定向发放） =====
export const grantCoupons = (d) => request.post('/admin/coupon/grant', d)
export const getGrantTask = (id) => request.get(`/admin/coupon/task/${id}`)
export const listGrantTasks = (params) => request.get('/admin/coupon/task/page', { params })
export const listGrantTaskDetails = (params) => request.get('/admin/coupon/task/detail/page', { params })