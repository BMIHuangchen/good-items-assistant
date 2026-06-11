import request from '@/utils/request'
import type { Banner, Category, DashboardStats, GoodItem, MiniProgramConfig, PageResult } from '@/types'

export interface LoginResult {
  token: string
  username: string
  displayName: string
  roles: string[]
}

export const api = {
  login: (username: string, password: string) => request.post<any, LoginResult>('/admin/auth/login', { username, password }),
  me: () => request.get<any, Record<string, any>>('/admin/auth/me'),
  dashboard: () => request.get<any, DashboardStats>('/admin/dashboard'),
  ready: () => request.get<any, Record<string, any>>('/diagnostics/ready'),
  miniConfig: () => request.get<any, MiniProgramConfig>('/admin/mini-config'),
  updateMiniConfig: (body: Partial<MiniProgramConfig>) => request.put<any, MiniProgramConfig>('/admin/mini-config', body),
  items: (params?: Record<string, any>) => request.get<any, PageResult<GoodItem>>('/admin/items', { params }),
  createItem: (body: Partial<GoodItem>) => request.post<any, GoodItem>('/admin/items', body),
  updateItem: (id: number, body: Partial<GoodItem>) => request.put<any, GoodItem>(`/admin/items/${id}`, body),
  categories: () => request.get<any, Category[]>('/admin/categories'),
  createCategory: (body: Partial<Category>) => request.post<any, Category>('/admin/categories', body),
  updateCategory: (id: number, body: Partial<Category>) => request.put<any, Category>(`/admin/categories/${id}`, body),
  banners: () => request.get<any, Banner[]>('/admin/banners'),
  createBanner: (body: Partial<Banner>) => request.post<any, Banner>('/admin/banners', body),
  updateBanner: (id: number, body: Partial<Banner>) => request.put<any, Banner>(`/admin/banners/${id}`, body),
}
