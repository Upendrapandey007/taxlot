"use client"
import Link from 'next/link'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { registerSchema } from '@/lib/validators/auth'
import { useAuth } from '@/lib/hooks/use-auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Spinner } from '@/components/ui/spinner'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { z } from 'zod'

type RegisterData = z.infer<typeof registerSchema>

export default function RegisterPage() {
  const { register: registerAuth } = useAuth()
  const router = useRouter()
  const [error, setError] = useState<string | null>(null)

  const { register, handleSubmit, watch, formState: { errors } } = useForm<RegisterData>({
    resolver: zodResolver(registerSchema)
  })

  const password = watch('password') || ''

  const getPasswordStrength = (pass: string) => {
    if (pass.length === 0) return { label: '', color: 'bg-transparent' }
    if (pass.length < 8) return { label: 'Weak', color: 'bg-destructive' }
    if (pass.length >= 8 && /[A-Z]/.test(pass) && /[0-9]/.test(pass)) return { label: 'Strong', color: 'bg-success' }
    return { label: 'Fair', color: 'bg-warning' }
  }

  const strength = getPasswordStrength(password)

  const onSubmit = (data: RegisterData) => {
    setError(null)
    registerAuth.mutate(data, {
      onSuccess: () => {
        router.push('/onboarding')
      },
      onError: (err: any) => {
        setError(err.message || 'An error occurred during registration')
      }
    })
  }

  return (
    <Card className="w-full">
      <CardHeader className="space-y-1 text-center">
        <CardTitle className="text-2xl">Create your account</CardTitle>
        <CardDescription>Start managing your finances in minutes</CardDescription>
      </CardHeader>
      <form onSubmit={handleSubmit(onSubmit)}>
        <CardContent className="space-y-4">
          {error && <div className="text-sm text-destructive">{error}</div>}
          <div className="space-y-2">
            <Label htmlFor="full_name">Full Name</Label>
            <Input id="full_name" {...register('full_name')} />
            {errors.full_name && <p className="text-xs text-destructive">{errors.full_name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" {...register('email')} />
            {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" {...register('password')} />
            {strength.label && (
              <div className="flex items-center space-x-2 mt-1">
                <div className="h-1 flex-1 bg-muted rounded overflow-hidden">
                  <div className={`h-full ${strength.color}`} style={{ width: strength.label === 'Weak' ? '33%' : strength.label === 'Fair' ? '66%' : '100%' }}></div>
                </div>
                <span className={`text-xs ${strength.color.replace('bg-', 'text-')}`}>{strength.label}</span>
              </div>
            )}
            {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirm_password">Confirm Password</Label>
            <Input id="confirm_password" type="password" {...register('confirm_password')} />
            {errors.confirm_password && <p className="text-xs text-destructive">{errors.confirm_password.message}</p>}
          </div>
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          <Button type="submit" className="w-full" disabled={registerAuth.isPending}>
            {registerAuth.isPending && <Spinner className="mr-2 h-4 w-4" />}
            Create Account
          </Button>
          <div className="text-sm text-center text-muted-foreground">
            Already have an account?{' '}
            <Link href="/login" className="text-primary hover:underline">Sign in</Link>
          </div>
        </CardFooter>
      </form>
    </Card>
  )
}
