import request from './request'

/** 登录，返回 { token, tokenName:'Authorization', userInfo } */
export function login(data) {
  return request.post('/auth/login', data)
}