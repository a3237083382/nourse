import { request } from './request'

export function getHome() {
  return request({ url: '/api/app/home' })
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
