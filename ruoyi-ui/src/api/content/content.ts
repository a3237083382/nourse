import request from '@/utils/request';

export function listContent(params: any) {
  return request({
    url: '/api/admin/content',
    method: 'get',
    params
  });
}

export function addContent(data: any) {
  return request({
    url: '/api/admin/content',
    method: 'post',
    data
  });
}

export function updateContent(id: string | number, data: any) {
  return request({
    url: `/api/admin/content/${id}`,
    method: 'put',
    data
  });
}

export function updateContentStatus(id: string | number, enabled: boolean) {
  return request({
    url: `/api/admin/content/${id}/status`,
    method: 'put',
    data: { enabled }
  });
}
