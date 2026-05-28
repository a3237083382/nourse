import request from '@/utils/request';

export function listCategory() {
  return request({
    url: '/api/admin/categories',
    method: 'get'
  });
}

export function addCategory(data: any) {
  return request({
    url: '/api/admin/categories',
    method: 'post',
    data
  });
}

export function updateCategory(id: string | number, data: any) {
  return request({
    url: `/api/admin/categories/${id}`,
    method: 'put',
    data
  });
}

export function updateCategoryStatus(id: string | number, enabled: boolean) {
  return request({
    url: `/api/admin/categories/${id}/status`,
    method: 'put',
    data: { enabled }
  });
}
