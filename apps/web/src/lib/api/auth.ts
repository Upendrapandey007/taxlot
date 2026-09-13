import { apiClient } from './client'
import { TokenResponse } from '@/types/api'

export const authApi = {
  register: (data: any) => apiClient.post<TokenResponse>('/api/v1/auth/register', data),
  login: (data: any) => apiClient.post<TokenResponse>('/api/v1/auth/login', data),
  refresh: (refresh_token: string) => apiClient.post<TokenResponse>('/api/v1/auth/refresh', { refresh_token }),
  logout: (refresh_token: string) => apiClient.post<void>('/api/v1/auth/logout', { refresh_token }),
}
