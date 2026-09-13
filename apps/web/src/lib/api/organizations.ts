import { apiClient } from './client'
import { OrganizationResponse } from '@/types/api'

export const organizationsApi = {
  createOrganization: (data: any) => apiClient.post<OrganizationResponse>('/api/v1/organizations', data),
  listOrganizations: () => apiClient.get<OrganizationResponse[]>('/api/v1/organizations'),
  getOrganization: (id: string) => apiClient.get<OrganizationResponse>(`/api/v1/organizations/${id}`),
}
