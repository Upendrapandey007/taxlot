"use client"
import { useQuery } from '@tanstack/react-query'
import { organizationsApi } from '@/lib/api/organizations'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'

export function OrgSwitcher() {
  const { data: orgs, isLoading } = useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationsApi.listOrganizations()
  })

  if (isLoading) {
    return <Skeleton className="h-10 w-full" />
  }

  if (!orgs || orgs.length === 0) {
    return null
  }

  return (
    <Select defaultValue={orgs[0]?.id}>
      <SelectTrigger className="w-full bg-muted/50 border-none font-medium">
        <SelectValue placeholder="Select organization" />
      </SelectTrigger>
      <SelectContent>
        {orgs.map((org: any) => (
          <SelectItem key={org.id} value={org.id}>
            {org.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
