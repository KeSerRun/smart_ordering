import request from './request'

export const listBanners = (params) => request.get('/admin/banner/page', { params })
export const updateBanner = (id, d) => request.put(`/admin/banner/${id}`, d)
export const updateBannerStatus = (id, status) => request.put(`/admin/banner/${id}/status`, null, { params: { status } })