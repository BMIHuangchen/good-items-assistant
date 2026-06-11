export interface Category {
  id: number
  name: string
  slug: string
  description: string
  coverImage: string
  sortOrder: number
  enabled: boolean
}

export interface GoodItem {
  id: number
  categoryId: number
  categoryName: string
  title: string
  summary: string
  experience: string
  tags: string[]
  coverImage: string
  gallery: string[]
  status: 'DRAFT' | 'PUBLISHED'
  sortOrder: number
  viewCount: number
  favoriteCount: number
}

export interface Banner {
  id: number
  title: string
  imageUrl: string
  targetType: string
  targetValue: string
  sortOrder: number
  enabled: boolean
}

export interface DashboardStats {
  publishedItems: number
  draftItems: number
  categories: number
  banners: number
  totalViews: number
  totalFavorites: number
}

export interface MiniProgramConfig {
  id: number
  heroEyebrow: string
  heroTitle: string
  heroSubtitle: string
  featuredTitle: string
  searchPlaceholder: string
  hotWords: string[]
  meTitle: string
  meDescription: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
}
