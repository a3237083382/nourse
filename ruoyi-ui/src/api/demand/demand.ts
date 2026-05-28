import request from '@/utils/request';

export function listDemand(params: any) {
  return request({
    url: '/api/admin/demands',
    method: 'get',
    params
  });
}

export function getDemand(id: string | number) {
  return request({
    url: `/api/admin/demands/${id}`,
    method: 'get'
  });
}

export function approveDemand(id: string | number) {
  return request({
    url: `/api/admin/demands/${id}/approve`,
    method: 'post'
  });
}

export function rejectDemand(id: string | number, data: any) {
  return request({
    url: `/api/admin/demands/${id}/reject`,
    method: 'post',
    data
  });
}

export function updateDemandFollowStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/demands/${id}/follow-status`,
    method: 'put',
    data
  });
}

export function addDemandRecommendation(id: string | number, data: any) {
  return request({
    url: `/api/admin/demands/${id}/recommendations`,
    method: 'post',
    data
  });
}

export function deleteDemandRecommendation(id: string | number) {
  return request({
    url: `/api/admin/demands/recommendations/${id}`,
    method: 'delete'
  });
}
