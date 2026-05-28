import request from '@/utils/request';

export function listInterview(params: any) {
  return request({
    url: '/api/admin/interviews',
    method: 'get',
    params
  });
}

export function getInterview(id: string | number) {
  return request({
    url: `/api/admin/interviews/${id}`,
    method: 'get'
  });
}

export function updateInterviewStatus(id: string | number, data: any) {
  return request({
    url: `/api/admin/interviews/${id}/status`,
    method: 'put',
    data
  });
}

export function updateInterviewNote(id: string | number, data: any) {
  return request({
    url: `/api/admin/interviews/${id}/note`,
    method: 'put',
    data
  });
}
