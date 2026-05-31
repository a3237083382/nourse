import request from '@/utils/request';

export function listGroupProduct(params: any) {
  return request({
    url: '/api/admin/group-products',
    method: 'get',
    params
  });
}

export function getGroupProduct(id: string | number) {
  return request({
    url: `/api/admin/group-products/${id}`,
    method: 'get'
  });
}

export function addGroupProduct(data: any) {
  return request({
    url: '/api/admin/group-products',
    method: 'post',
    data
  });
}

export function updateGroupProduct(id: string | number, data: any) {
  return request({
    url: `/api/admin/group-products/${id}`,
    method: 'put',
    data
  });
}

export function updateGroupProductStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/group-products/${id}/status`,
    method: 'put',
    data
  });
}

export function listGroupTeam(params: any) {
  return request({
    url: '/api/admin/group-teams',
    method: 'get',
    params
  });
}

export function updateGroupTeamStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/group-teams/${id}/status`,
    method: 'put',
    data
  });
}

export function listGroupOrder(params: any) {
  return request({
    url: '/api/admin/group-orders',
    method: 'get',
    params
  });
}

export function getGroupOrder(id: string | number) {
  return request({
    url: `/api/admin/group-orders/${id}`,
    method: 'get'
  });
}

export function updateGroupOrderStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/group-orders/${id}/status`,
    method: 'put',
    data
  });
}
