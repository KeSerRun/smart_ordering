import request from './request'

export const listMembers = (params) => request.get('/admin/member/page', { params })
export const memberOverview = () => request.get('/admin/member/overview')
export const memberLevels = () => request.get('/admin/member/level/list')
export const createMemberLevel = (d) => request.post('/admin/member/level', d)
export const updateMemberLevel = (id, d) => request.put(`/admin/member/level/${id}`, d)
export const updateMemberLevelStatus = (id, status) => request.put(`/admin/member/level/${id}/status`, null, { params: { status } })
export const listMemberPoints = (params) => request.get('/admin/member/points-record/page', { params })
export const listMemberGrowth = (params) => request.get('/admin/member/growth-record/page', { params })
export const adjustMemberPoints = (id, d) => request.post(`/admin/member/${id}/points-adjust`, d)
export const assignMemberLevel = (id, d) => request.put(`/admin/member/${id}/level`, d)