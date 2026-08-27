import request from './request'

// 桌区
export const listAreas = () => request.get('/admin/table/area/list')
export const createArea = (d) => request.post('/admin/table/area', d)
export const updateArea = (id, d) => request.put(`/admin/table/area/${id}`, d)
export const deleteArea = (id) => request.delete(`/admin/table/area/${id}`)

// 桌台
export const listTables = () => request.get('/admin/table/list')
export const createTable = (d) => request.post('/admin/table', d)
export const updateTable = (id, d) => request.put(`/admin/table/${id}`, d)
export const deleteTable = (id) => request.delete(`/admin/table/${id}`)
export const cleanTable = (id) => request.put(`/admin/table/${id}/clean`)
export const releaseTable = (id) => request.put(`/admin/table/${id}/release`)

// 桌台二维码
export const genAllQrTask = () => request.post('/admin/table/qrcode/generate-all/task')
export const getQrTask = (taskId) => request.get(`/admin/table/qrcode/task/${taskId}`)
export const downloadTableQr = (id) => request.get(`/admin/table/${id}/qrcode/download`, { responseType: 'blob' })