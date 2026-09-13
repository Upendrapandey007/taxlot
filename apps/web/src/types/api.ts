export interface TokenResponse {
  access_token: string
  refresh_token: string
  token_type: string
  expires_in: number
}

export interface UserResponse {
  id: string
  email: string
  full_name: string
  created_at: string
}

export interface OrganizationResponse {
  id: string
  name: string
  industry: IndustryType
  country_code: string
  currency_code: string
  created_at: string
}

export type IndustryType = 'RETAIL' | 'AGENCY' | 'FREELANCER' | 'RESTAURANT' | 'SERVICE' | 'OTHER'
export type MemberRole = 'OWNER' | 'ADMIN' | 'ACCOUNTANT' | 'STAFF' | 'AUDITOR'

export interface ApiError {
  code: string
  message: string
  request_id?: string
}
