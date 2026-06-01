export const BASE_URL = 'http://localhost:8081'

export function getToken() {
  return uni.getStorageSync('appToken') || ''
}

export function setToken(token) {
  uni.setStorageSync('appToken', token)
}

function cleanData(data) {
  if (!data || Array.isArray(data) || typeof data !== 'object') {
    return data || {}
  }
  return Object.keys(data).reduce((result, key) => {
    if (data[key] !== undefined) {
      result[key] = data[key]
    }
    return result
  }, {})
}

export function request(options) {
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: cleanData(options.data),
      header: {
        'content-type': 'application/json;charset=utf-8',
        ...(token ? { Authorization: `Bearer ${token}`, clientid: 'app' } : {}),
        ...(options.header || {}),
      },
      success(res) {
        const data = res.data || {}
        if (data.code === 401) {
          uni.removeStorageSync('appToken')
          reject(new Error(data.msg || '请先登录'))
          return
        }
        if (data.code && data.code !== 200) {
          reject(new Error(data.msg || '请求失败'))
          return
        }
        resolve(data)
      },
      fail(err) {
        reject(err)
      },
    })
  })
}

async function mockLogin() {
  let openid = uni.getStorageSync('mockOpenid')
  if (!openid) {
    openid = 'mock-openid'
    uni.setStorageSync('mockOpenid', openid)
  }
  const login = await request({
    url: '/api/app/auth/mock-login',
    method: 'POST',
    data: {
      openid,
      nickname: '小程序用户',
    },
  })
  setToken(login.data.accessToken)
}

export async function phoneLogin(detail = {}) {
  const login = await request({
    url: '/api/app/auth/phone-login',
    method: 'POST',
    data: {
      code: detail.code,
      phone: detail.phone,
    },
  })
  setToken(login.data.accessToken)
  return login.data
}

export function ensureLogin(options = {}) {
  if (getToken() && !options.refresh) {
    return Promise.resolve()
  }
  if (options.refresh) {
    return mockLogin()
  }
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: '登录提示',
      content: '请先登录后继续操作',
      confirmText: '去登录',
      success: async (res) => {
        if (!res.confirm) {
          reject(new Error('cancel login'))
          return
        }
        try {
          await mockLogin()
          resolve()
        } catch (error) {
          reject(error)
        }
      },
    })
  })
}
