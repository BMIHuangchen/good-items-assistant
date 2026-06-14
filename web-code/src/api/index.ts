import request from '@/utils/request'
import type { AiCallLog, AiFeatureSettings, AiImageAnalysisTask, AiModelConfig, Banner, Category, DashboardStats, GoodItem, MiniProgramConfig, PageResult } from '@/types'

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
  aiSettings: () => request.get<any, AiFeatureSettings>('/admin/ai/settings'),
  updateAiSettings: (body: Partial<AiFeatureSettings>) => request.put<any, AiFeatureSettings>('/admin/ai/settings', body),
  aiModels: () => request.get<any, AiModelConfig[]>('/admin/ai/models'),
  updateAiModel: (id: number, body: Partial<AiModelConfig>) => request.put<any, AiModelConfig>(`/admin/ai/models/${id}`, body),
  aiImageTasks: (params?: Record<string, any>) => request.get<any, AiImageAnalysisTask[]>('/admin/ai/image-tasks', { params }),
  confirmAiImageTask: (id: number, body: Record<string, any>) => request.post<any, AiImageAnalysisTask>(`/admin/ai/image-tasks/${id}/confirm`, body),
  rejectAiImageTask: (id: number, reason: string) => request.post<any, void>(`/admin/ai/image-tasks/${id}/reject`, { reason }),
  aiCallLogs: (params?: Record<string, any>) => request.get<any, AiCallLog[]>('/admin/ai/call-logs', { params }),
}
