import { z } from 'zod'

export const registerSchema = z.object({
  full_name: z.string().min(2, 'Name must be at least 2 characters').max(100),
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters').max(100),
  confirm_password: z.string()
}).refine(d => d.password === d.confirm_password, {
  message: "Passwords don't match",
  path: ['confirm_password']
})

export const loginSchema = z.object({
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(1, 'Password is required')
})

export const createOrganizationSchema = z.object({
  name: z.string().min(2).max(255),
  industry: z.enum(['RETAIL', 'AGENCY', 'FREELANCER', 'RESTAURANT', 'SERVICE', 'OTHER']),
  country_code: z.string().length(2),
  currency_code: z.string().length(3),
  tax_number: z.string().optional()
})
