import request from '@/utils/request';

export function listStaff(params: any) {
  return request({
    url: '/api/admin/staff',
    method: 'get',
    params
  });
}

export function getStaff(id: string | number) {
  return request({
    url: `/api/admin/staff/${id}`,
    method: 'get'
  });
}

export function addStaff(data: any) {
  return request({
    url: '/api/admin/staff',
    method: 'post',
    data
  });
}

export function updateStaff(id: string | number, data: any) {
  return request({
    url: `/api/admin/staff/${id}`,
    method: 'put',
    data
  });
}

export function updateStaffStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/staff/${id}/status`,
    method: 'put',
    data
  });
}
