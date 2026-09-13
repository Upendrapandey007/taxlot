"use client"
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { createOrganizationSchema } from '@/lib/validators/auth'
import { organizationsApi } from '@/lib/api/organizations'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Spinner } from '@/components/ui/spinner'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ShoppingBag, Briefcase, User, UtensilsCrossed, Settings, MoreHorizontal } from 'lucide-react'
import { z } from 'zod'

type OrgData = z.infer<typeof createOrganizationSchema>

const industries = [
  { value: 'RETAIL', label: 'Retail', icon: ShoppingBag },
  { value: 'AGENCY', label: 'Agency', icon: Briefcase },
  { value: 'FREELANCER', label: 'Freelancer', icon: User },
  { value: 'RESTAURANT', label: 'Restaurant', icon: UtensilsCrossed },
  { value: 'SERVICE', label: 'Service Business', icon: Settings },
  { value: 'OTHER', label: 'Other', icon: MoreHorizontal },
]

export default function OnboardingPage() {
  const router = useRouter()
  const queryClient = useQueryClient()
  const [step, setStep] = useState(1)

  const { register, handleSubmit, control, watch, setValue, formState: { errors } } = useForm<OrgData>({
    resolver: zodResolver(createOrganizationSchema),
    defaultValues: {
      country_code: 'US',
      currency_code: 'USD'
    }
  })

  const selectedIndustry = watch('industry')

  const createOrg = useMutation({
    mutationFn: organizationsApi.createOrganization,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizations'] })
      router.push('/dashboard')
    }
  })

  const onSubmit = (data: OrgData) => {
    if (step < 3) {
      setStep(step + 1)
    } else {
      createOrg.mutate(data)
    }
  }

  return (
    <Card className="w-full max-w-lg mx-auto">
      <CardHeader>
        <CardTitle>Business Setup</CardTitle>
        <CardDescription>Step {step} of 3</CardDescription>
        <div className="w-full bg-muted h-2 rounded overflow-hidden mt-2">
          <div className="bg-primary h-full transition-all" style={{ width: `${(step / 3) * 100}%` }} />
        </div>
      </CardHeader>
      <form onSubmit={handleSubmit(onSubmit)}>
        <CardContent className="space-y-4">
          {step === 1 && (
            <>
              <div className="space-y-2">
                <Label htmlFor="name">Business Name *</Label>
                <Input id="name" {...register('name')} />
                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Country *</Label>
                  <Controller
                    name="country_code"
                    control={control}
                    render={({ field }) => (
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select country" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="US">United States (US)</SelectItem>
                          <SelectItem value="GB">United Kingdom (GB)</SelectItem>
                          <SelectItem value="CA">Canada (CA)</SelectItem>
                          <SelectItem value="AU">Australia (AU)</SelectItem>
                          <SelectItem value="IN">India (IN)</SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                  />
                  {errors.country_code && <p className="text-xs text-destructive">{errors.country_code.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label>Currency *</Label>
                  <Controller
                    name="currency_code"
                    control={control}
                    render={({ field }) => (
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select currency" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="USD">USD ($)</SelectItem>
                          <SelectItem value="GBP">GBP (£)</SelectItem>
                          <SelectItem value="EUR">EUR (€)</SelectItem>
                          <SelectItem value="CAD">CAD ($)</SelectItem>
                          <SelectItem value="AUD">AUD ($)</SelectItem>
                          <SelectItem value="INR">INR (₹)</SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                  />
                  {errors.currency_code && <p className="text-xs text-destructive">{errors.currency_code.message}</p>}
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="tax_number">Tax/VAT Number (Optional)</Label>
                <Input id="tax_number" {...register('tax_number')} />
              </div>
            </>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <Label>Select your business type</Label>
              <div className="grid grid-cols-2 gap-4">
                {industries.map(ind => {
                  const Icon = ind.icon
                  return (
                    <div
                      key={ind.value}
                      className={`cursor-pointer border rounded-lg p-4 flex flex-col items-center gap-2 hover:border-primary transition-colors ${selectedIndustry === ind.value ? 'border-primary bg-primary/5' : ''}`}
                      onClick={() => {
                        setValue('industry', ind.value as any, { shouldValidate: true })
                      }}
                    >
                      <Icon className="w-8 h-8 text-muted-foreground" />
                      <span className="text-sm font-medium">{ind.label}</span>
                    </div>
                  )
                })}
              </div>
              {errors.industry && <p className="text-xs text-destructive">{errors.industry.message}</p>}
            </div>
          )}

          {step === 3 && (
            <div className="space-y-4">
              <Label>Invite a team member (Optional)</Label>
              <p className="text-sm text-muted-foreground">You can add your accountant or staff later.</p>
              <div className="space-y-2">
                <Label>Email</Label>
                <Input type="email" placeholder="team@example.com" />
              </div>
              <div className="space-y-2">
                <Label>Role</Label>
                <Select defaultValue="STAFF">
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                    <SelectItem value="ACCOUNTANT">Accountant</SelectItem>
                    <SelectItem value="STAFF">Staff</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}
        </CardContent>
        <CardFooter className="flex justify-between">
          {step > 1 ? (
            <Button type="button" variant="outline" onClick={() => setStep(step - 1)}>Back</Button>
          ) : <div></div>}
          
          <div className="flex space-x-2">
            {step === 3 && (
              <Button type="button" variant="ghost" onClick={() => createOrg.mutate(watch())}>
                Skip for now
              </Button>
            )}
            <Button type="submit" disabled={createOrg.isPending}>
              {createOrg.isPending && <Spinner className="mr-2 h-4 w-4" />}
              {step < 3 ? 'Continue' : 'Complete Setup'}
            </Button>
          </div>
        </CardFooter>
      </form>
    </Card>
  )
}
