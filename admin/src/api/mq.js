import request from './request'

// ===== MQ 可靠消息（发件箱）=====
export const listMqMessages = (params) => request.get('/admin/mq/message/page', { params })
export const retryMqMessage = (id) => request.post(`/admin/mq/message/${id}/retry`)