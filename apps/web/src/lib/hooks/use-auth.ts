import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import { usersApi } from '../api/users'
import Cookies from 'js-cookie'

export function useAuth() {
  const queryClient = useQueryClient()

  const { data: user, isLoading } = useQuery({
    queryKey: ['user'],
    queryFn: () => usersApi.getCurrentUser(),
    retry: false,
    staleTime: 5 * 60 * 1000,
  })

  const login = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      localStorage.setItem('taxlot_access_token', data.access_token)
      localStorage.setItem('taxlot_refresh_token', data.refresh_token)
      Cookies.set('auth', 'true')
      queryClient.invalidateQueries({ queryKey: ['user'] })
    },
  })

  const register = useMutation({
    mutationFn: authApi.register,
    onSuccess: (data) => {
      localStorage.setItem('taxlot_access_token', data.access_token)
      localStorage.setItem('taxlot_refresh_token', data.refresh_token)
      Cookies.set('auth', 'true')
      queryClient.invalidateQueries({ queryKey: ['user'] })
    },
  })

  const logout = useMutation({
    mutationFn: () => {
      const refreshToken = localStorage.getItem('taxlot_refresh_token') || ''
      return authApi.logout(refreshToken)
    },
    onSettled: () => {
      localStorage.removeItem('taxlot_access_token')
      localStorage.removeItem('taxlot_refresh_token')
      Cookies.remove('auth')
      queryClient.setQueryData(['user'], null)
      window.location.href = '/login'
    },
  })

  return {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
  }
}
