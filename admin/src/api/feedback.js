import request from './request'

export const listFeedback = (params) => request.get('/admin/feedback/list', { params })
export const replyFeedback = (id, d) => request.put(`/admin/feedback/${id}/reply`, d)