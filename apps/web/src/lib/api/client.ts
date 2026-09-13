import Cookies from 'js-cookie'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export class ApiClientError extends Error {
  code: string
  status: number
  request_id?: string

  constructor(status: number, message: string, code: string, request_id?: string) {
    super(message)
    this.name = 'ApiClientError'
    this.status = status
    this.code = code
    this.request_id = request_id
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorData
    try {
      errorData = await response.json()
    } catch {
      errorData = { message: 'An unknown error occurred', code: 'UNKNOWN_ERROR' }
    }
    throw new ApiClientError(response.status, errorData.message, errorData.code, errorData.request_id)
  }
  
  if (response.status === 204) {
    return {} as T
  }
  
  return response.json()
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('taxlot_access_token')
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  }

  if (token) {
    headers['Authorization'] = \`Bearer \${token}\`
  }

  let response = await fetch(\`\${API_URL}\${endpoint}\`, { ...options, headers })

  if (response.status === 401) {
    const refreshToken = localStorage.getItem('taxlot_refresh_token')
    if (refreshToken) {
      try {
        const refreshRes = await fetch(\`\${API_URL}/api/v1/auth/refresh\`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refresh_token: refreshToken }),
        })
        if (refreshRes.ok) {
          const data = await refreshRes.json()
          localStorage.setItem('taxlot_access_token', data.access_token)
          localStorage.setItem('taxlot_refresh_token', data.refresh_token)
          Cookies.set('auth', 'true')
          headers['Authorization'] = \`Bearer \${data.access_token}\`
          response = await fetch(\`\${API_URL}\${endpoint}\`, { ...options, headers })
        } else {
          throw new Error('Refresh failed')
        }
      } catch (e) {
        localStorage.removeItem('taxlot_access_token')
        localStorage.removeItem('taxlot_refresh_token')
        Cookies.remove('auth')
        window.location.href = '/login'
      }
    } else {
      localStorage.removeItem('taxlot_access_token')
      localStorage.removeItem('taxlot_refresh_token')
      Cookies.remove('auth')
      window.location.href = '/login'
    }
  }

  return handleResponse<T>(response)
}

export const apiClient = {
  get: <T>(endpoint: string, options?: RequestInit) => request<T>(endpoint, { ...options, method: 'GET' }),
  post: <T>(endpoint: string, body?: any, options?: RequestInit) =>
    request<T>(endpoint, { ...options, method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(endpoint: string, body: any, options?: RequestInit) =>
    request<T>(endpoint, { ...options, method: 'PATCH', body: JSON.stringify(body) }),
  put: <T>(endpoint: string, body: any, options?: RequestInit) =>
    request<T>(endpoint, { ...options, method: 'PUT', body: JSON.stringify(body) }),
  delete: <T>(endpoint: string, options?: RequestInit) => request<T>(endpoint, { ...options, method: 'DELETE' }),
}
