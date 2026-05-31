import request from '@/utils/request';

export function listContract(params: any) {
  return request({
    url: '/api/admin/contracts',
    method: 'get',
    params
  });
}

export function getContract(id: string | number) {
  return request({
    url: `/api/admin/contracts/${id}`,
    method: 'get'
  });
}

export function addContract(data: any) {
  return request({
    url: '/api/admin/contracts',
    method: 'post',
    data
  });
}

export function updateContract(id: string | number, data: any) {
  return request({
    url: `/api/admin/contracts/${id}`,
    method: 'put',
    data
  });
}

export function updateContractStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/contracts/${id}/status`,
    method: 'put',
    data
  });
}
