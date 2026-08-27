import request from './request'

// ===== 用户 =====
export const listUsers = (params) => request.get('/system/user/page', { params })
export const updateUserStatus = (userId, status) => request.put(`/system/user/${userId}/status/${status}`)

// ===== 角色 =====
export const listRoles = (params) => request.get('/system/role/page', { params })
export const updateRoleStatus = (roleId, status) => request.put(`/system/role/${roleId}/status/${status}`)

// ===== 菜单 =====
export const listMenus = (params) => request.get('/system/menu/list', { params })

// ===== 字典类型 =====
export const listDictTypes = (params) => request.get('/system/dict/type/page', { params })
// ===== 字典数据 =====
export const listDictDatas = (typeId) => request.get(`/system/dict/data/type/${typeId}`)
export const fetchDictDataByCode = (code) => request.get(`/system/dict/data/code/${code}`)

// ===== 配置 =====
export const listConfigs = (params) => request.get('/system/config/page', { params })

// ===== 日志 =====
export const listLoginLogs = (params) => request.get('/system/log/login/page', { params })
export const listOperationLogs = (params) => request.get('/system/log/operation/page', { params })