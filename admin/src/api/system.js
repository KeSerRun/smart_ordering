import request from './request'

// ===== 用户 =====
export const listUsers = (params) => request.get('/system/user/page', { params })
export const createUser = (d) => request.post('/system/user', d)
export const updateUser = (id, d) => request.put(`/system/user/${id}`, d)
export const deleteUser = (id) => request.delete(`/system/user/${id}`)
export const updateUserStatus = (userId, status) => request.put(`/system/user/${userId}/status/${status}`)
export const updateUserModules = (userId, modules) => request.put(`/system/user/${userId}/modules`, modules)
export const resetUserPassword = (userId, newPassword) => request.put(`/system/user/${userId}/password/reset`, { newPassword })

// ===== 角色 =====
export const listRoles = (params) => request.get('/system/role/page', { params })
export const updateRoleStatus = (roleId, status) => request.put(`/system/role/${roleId}/status/${status}`)

// ===== 日志 =====
export const listLoginLogs = (params) => request.get('/system/log/login/page', { params })
export const listOperationLogs = (params) => request.get('/system/log/operation/page', { params })