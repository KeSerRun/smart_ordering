import request from './request'

// ===== 用户（账户 + 角色分配；权限由角色管理配置） =====
export const listUsers = (params) => request.get('/system/user/page', { params })
export const createUser = (d) => request.post('/system/user', d)
export const updateUser = (id, d) => request.put(`/system/user/${id}`, d)
export const deleteUser = (id) => request.delete(`/system/user/${id}`)
export const updateUserStatus = (userId, status) => request.put(`/system/user/${userId}/status/${status}`)
export const resetUserPassword = (userId, newPassword) => request.put(`/system/user/${userId}/password/reset`, { newPassword })

// ===== 角色（角色上配置模块权限） =====
export const listRoles = (params) => request.get('/system/role/page', { params })
export const listAllRoles = () => request.get('/system/role/list')
export const createRole = (d) => request.post('/system/role', d)
export const updateRole = (d) => request.put('/system/role', d)
export const deleteRole = (roleId) => request.delete(`/system/role/${roleId}`)
export const updateRoleStatus = (roleId, status) => request.put(`/system/role/${roleId}/status/${status}`)

// ===== 日志 =====
export const listLoginLogs = (params) => request.get('/system/log/login/page', { params })
export const listOperationLogs = (params) => request.get('/system/log/operation/page', { params })