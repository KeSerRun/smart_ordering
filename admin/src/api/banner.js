import request from './request'

export const listBanners = (params) => request.get('/admin/banner/page', { params })
export const createBanner = (d) => request.post('/admin/banner', d)
export const updateBanner = (id, d) => request.put(`/admin/banner/${id}`, d)
export const updateBannerStatus = (id, status) => request.put(`/admin/banner/${id}/status`, null, { params: { status } })
export const deleteBanner = (id) => request.delete(`/admin/banner/${id}`)

// 轮播图上传（MinIO，返回 { url, objectName }）
export const uploadBannerImage = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/file/upload/banner-image', fd)
}