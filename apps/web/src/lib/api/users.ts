import { apiClient } from './client'
import { UserResponse } from '@/types/api'

export const usersApi = {
  getCurrentUser: () => apiClient.get<UserResponse>('/api/v1/users/me'),
}
