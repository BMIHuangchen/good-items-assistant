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

export interface AiFeatureSettings {
  id: number
  aiEnabled: boolean
  autoIngestEnabled: boolean
  autoPublishEnabled: boolean
  lowConfidenceReviewEnabled: boolean
  confidenceThreshold: number
  dailyCallLimit: number
  maxImageSizeMb: number
}

export interface AiModelConfig {
  id: number
  providerCode: string
  displayName: string
  modelName: string
  baseUrl: string
  apiKeyEnv: string
  enabled: boolean
  promptPricePer1k: number
  completionPricePer1k: number
  sortOrder: number
}

export interface AiImageAnalysisTask {
  id: number
  requestId: string
  userId?: number
  openidMask?: string
  mediaAssetId: number
  mediaUrl: string
  providerCode: string
  modelName: string
  status: string
  ingestMode: string
  itemTitle: string
  summary: string
  experience: string
  tags: string[]
  decision: string
  matchedCategoryId?: number
  matchedCategoryName?: string
  newCategoryName?: string
  newCategorySlug?: string
  newCategoryDescription?: string
  confidence: number
  reason: string
  reviewReason?: string
  createdCategoryId?: number
  createdItemId?: number
}

export interface AiCallLog {
  id: number
  requestId: string
  userId?: number
  openidMask?: string
  providerCode: string
  modelName: string
  scenario: string
  status: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  estimatedCost: number
  durationMs: number
  errorMessage?: string
  taskId?: number
  createdAt: string
}

export interface MiniUser {
  id: number
  openid: string
  unionid?: string
  nickname?: string
  avatarUrl?: string
  status: string
  tierCode: string
  customDailyTokenLimit?: number
  customMonthlyTokenLimit?: number
  customDailyCallLimit?: number
  loginCount: number
  firstLoginAt?: string
  lastLoginAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface UserAiUsage {
  userId: number
  openidMask: string
  nickname?: string
  tierCode: string
  tierName: string
  callCount: number
  successCount: number
  failedCount: number
  promptTokens: number
  completionTokens: number
  totalTokens: number
  estimatedCost: number
  avgDurationMs: number
}

export interface AnalyticsOverview {
  totalUsers: number
  todayLogins: number
  todayActiveUsers: number
  totalBehaviorEvents: number
  totalAiCalls: number
  totalAiTokens: number
  totalAiEstimatedCost: number
  loginTrend: Array<{ date: string; value: number }>
  aiTrend: Array<{ date: string; value: number }>
  aiTokenTrend: Array<{ date: string; value: number }>
  eventRanking: Array<{ name: string; value: number }>
  modelUsage: Array<{ name: string; value: number }>
  modelComputeUsage: Array<{
    providerCode: string
    modelName: string
    callCount: number
    successCount: number
    failedCount: number
    promptTokens: number
    completionTokens: number
    totalTokens: number
    estimatedCost: number
    avgDurationMs: number
  }>
  userComputeRanking: Array<{
    userId: number
    openidMask: string
    nickname?: string
    tierCode: string
    tierName: string
    callCount: number
    totalTokens: number
    estimatedCost: number
  }>
  hotItems: Array<{ name: string; value: number }>
}

export interface ComputeTier {
  tierCode: string
  tierName: string
  dailyTokenLimit: number
  monthlyTokenLimit: number
  dailyCallLimit: number
  enabled: boolean
  sortOrder: number
  updatedAt?: string
}
