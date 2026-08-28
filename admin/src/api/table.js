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
export const deleteTableQr = (id) => request.delete(`/admin/table/${id}/qrcode`)

// 打包下载全部二维码
export const genDownloadAllQrTask = () => request.post('/admin/table/qrcode/download-all/task')
export const downloadQrTaskFile = (taskId) => request.get(`/admin/table/qrcode/task/${taskId}/download`, { responseType: 'blob' })

// 桌台点餐（开台下单，直接传菜品明细）
export const createTableOrder = (d) => request.post('/admin/order', d)