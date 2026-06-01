import { request } from './request'

export function getHome() {
  return request({ url: '/api/app/home' })
}

export function getContent(type) {
  return request({ url: `/api/app/content/${type}` })
}

export function getUserProfile() {
  return request({ url: '/api/app/user/profile' })
}

export function updateUserProfile(data) {
  return request({ url: '/api/app/user/profile', method: 'PUT', data })
}

export function getCategories() {
  return request({ url: '/api/app/categories' })
}

export function getStaffList(params) {
  return request({ url: '/api/app/staff', data: params })
}

export function getStaffDetail(id) {
  return request({ url: `/api/app/staff/${id}` })
}

export function favoriteStaff(id) {
  return request({ url: `/api/app/staff/${id}/favorite`, method: 'POST' })
}

export function cancelFavoriteStaff(id) {
  return request({ url: `/api/app/staff/${id}/favorite`, method: 'DELETE' })
}

export function getFavoriteStaff(params) {
  return request({ url: '/api/app/favorites/staff', data: params })
}

export function createDemand(data) {
  return request({ url: '/api/app/demands', method: 'POST', data })
}

export function getDemandList(params) {
  return request({ url: '/api/app/demands', data: params })
}

export function getDemandDetail(id) {
  return request({ url: `/api/app/demands/${id}` })
}

export function cancelDemand(id) {
  return request({ url: `/api/app/demands/${id}/cancel`, method: 'POST' })
}

export function getDemandRecommendations(id) {
  return request({ url: `/api/app/demands/${id}/recommendations` })
}

export function createInterview(data) {
  return request({ url: '/api/app/interviews', method: 'POST', data })
}

export function getInterviewList(params) {
  return request({ url: '/api/app/interviews', data: params })
}

export function getInterviewDetail(id) {
  return request({ url: `/api/app/interviews/${id}` })
}

export function getMessageList(params) {
  return request({ url: '/api/app/messages', data: params })
}

export function markMessageRead(id) {
  return request({ url: `/api/app/messages/${id}/read`, method: 'POST' })
}

export function getContractList(params) {
  return request({ url: '/api/app/contracts', data: params })
}

export function getContractDetail(id) {
  return request({ url: `/api/app/contracts/${id}` })
}

export function getServiceOrderList(params) {
  return request({ url: '/api/app/service-orders', data: params })
}

export function getServiceOrderDetail(id) {
  return request({ url: `/api/app/service-orders/${id}` })
}

export function reviewServiceOrder(id, data) {
  return request({ url: `/api/app/service-orders/${id}/review`, method: 'POST', data })
}

export function getGroupProductList(params) {
  return request({ url: '/api/app/group-products', data: params })
}

export function getGroupProductDetail(id) {
  return request({ url: `/api/app/group-products/${id}` })
}

export function createSingleGroupOrder(data) {
  return request({ url: '/api/app/group-orders/single', method: 'POST', data })
}

export function startGroupOrder(data) {
  return request({ url: '/api/app/group-orders/group/start', method: 'POST', data })
}

export function joinGroupOrder(data) {
  return request({ url: '/api/app/group-orders/group/join', method: 'POST', data })
}

export function getGroupOrderList(params) {
  return request({ url: '/api/app/group-orders', data: params })
}

export function getGroupOrderDetail(id) {
  return request({ url: `/api/app/group-orders/${id}` })
}

export function reviewGroupOrder(id, data) {
  return request({ url: `/api/app/group-orders/${id}/review`, method: 'POST', data })
}
