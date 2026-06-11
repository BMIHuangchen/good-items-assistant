import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  requestId: string
}

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  <T>(response: AxiosResponse<ApiResponse<T>>) => {
    const { code, message, data, requestId } = response.data
    
    // 请求成功
    if (code === 200) {
      return data as T
    }

    if (code === 401) {
      ElMessage.error(message ? `${message}${requestId ? `，排查编号：${requestId}` : ''}` : '登录状态已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/'
      return Promise.reject(new Error(message || '登录状态已过期'))
    }
    
    // 业务错误
    ElMessage.error(message ? `${message}${requestId ? `，排查编号：${requestId}` : ''}` : '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    // HTTP错误处理
    const { response } = error
    
    if (response) {
      const { status, data } = response
      const requestId = data?.requestId || response.headers?.['x-request-id']
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || `服务器内部错误${requestId ? `，排查编号：${requestId}` : ''}`)
          break
        default:
          ElMessage.error(data?.message || `网络错误${requestId ? `，排查编号：${requestId}` : ''}`)
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
