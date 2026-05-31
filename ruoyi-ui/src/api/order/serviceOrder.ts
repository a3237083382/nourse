import request from '@/utils/request';

export function listServiceOrder(params: any) {
  return request({
    url: '/api/admin/service-orders',
    method: 'get',
    params
  });
}

export function getServiceOrder(id: string | number) {
  return request({
    url: `/api/admin/service-orders/${id}`,
    method: 'get'
  });
}

export function addServiceOrder(data: any) {
  return request({
    url: '/api/admin/service-orders',
    method: 'post',
    data
  });
}

export function updateServiceOrder(id: string | number, data: any) {
  return request({
    url: `/api/admin/service-orders/${id}`,
    method: 'put',
    data
  });
}

export function updateServiceOrderStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/service-orders/${id}/status`,
    method: 'put',
    data
  });
}
