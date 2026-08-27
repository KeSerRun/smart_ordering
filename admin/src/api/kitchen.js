import request from './request'

// 后厨
export const listKitchenTasks = () => request.get('/app/kitchen/tasks')
export const acceptTask = (itemId) => request.put(`/app/kitchen/task/${itemId}/accept`)
export const completeTask = (itemId) => request.put(`/app/kitchen/task/${itemId}/complete`)
export const getAutoAccept = () => request.get('/app/kitchen/auto-accept')
export const setAutoAccept = (enabled) => request.put('/app/kitchen/auto-accept', null, { params: { enabled } })