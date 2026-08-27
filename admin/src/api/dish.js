import request from './request'

// 分类
export const listCategories = () => request.get('/admin/dish/category/list')
export const createCategory = (d) => request.post('/admin/dish/category', d)
export const updateCategory = (id, d) => request.put(`/admin/dish/category/${id}`, d)
export const deleteCategory = (id) => request.delete(`/admin/dish/category/${id}`)

// 规格组
export const listSpecGroups = () => request.get('/admin/dish/spec/list')
export const createSpecGroup = (d) => request.post('/admin/dish/spec', d)
export const updateSpecGroup = (id, d) => request.put(`/admin/dish/spec/${id}`, d)
export const deleteSpecGroup = (id) => request.delete(`/admin/dish/spec/${id}`)

// 菜品
export const listDishes = (params) => request.get('/admin/dish/list', { params })
export const dishDetail = (id) => request.get(`/admin/dish/${id}`)
export const createDish = (d) => request.post('/admin/dish', d)
export const updateDish = (id, d) => request.put(`/admin/dish/${id}`, d)
export const updateDishStatus = (id, status) => request.put(`/admin/dish/${id}/status`, null, { params: { status } })