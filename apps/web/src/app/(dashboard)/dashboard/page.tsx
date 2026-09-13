"use client"
import Link from 'next/link'
import { useAuth } from '@/lib/hooks/use-auth'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Receipt, FileText, BarChart3, Calculator, TrendingUp, TrendingDown, Clock, Wallet, CheckCircle2, Circle, Activity, Sparkles } from 'lucide-react'

export default function DashboardPage() {
  const { user } = useAuth()
  const currentHour = new Date().getHours()
  const greeting = currentHour < 12 ? 'Good morning' : currentHour < 18 ? 'Good afternoon' : 'Good evening'

  return (
    <>
      <div>
        <h2 className="text-2xl font-bold tracking-tight">{greeting}, {user?.full_name?.split(' ')[0]}</h2>
        <p className="text-muted-foreground mt-1">Here&apos;s what&apos;s happening with your business.</p>
      </div>

      <Card>
        <CardHeader className="pb-4">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Get started with Taxlot</CardTitle>
              <CardDescription>Complete these steps to set up your account</CardDescription>
            </div>
            <div className="text-sm font-medium text-muted-foreground">1/5 completed</div>
          </div>
          <div className="w-full bg-muted h-2 rounded overflow-hidden mt-4">
            <div className="bg-primary h-full transition-all" style={{ width: '20%' }} />
          </div>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center gap-3 opacity-50">
            <CheckCircle2 className="h-5 w-5 text-primary" />
            <span className="text-sm line-through">Create your account</span>
          </div>
          <div className="flex items-center gap-3">
            <Circle className="h-5 w-5 text-muted-foreground" />
            <span className="text-sm font-medium">Add your first expense</span>
            <Button variant="link" className="h-auto p-0 ml-auto" asChild>
              <Link href="/expenses">Record Expense</Link>
            </Button>
          </div>
          <div className="flex items-center gap-3">
            <Circle className="h-5 w-5 text-muted-foreground" />
            <span className="text-sm font-medium">Create an invoice</span>
            <Button variant="link" className="h-auto p-0 ml-auto" asChild>
              <Link href="/invoices">New Invoice</Link>
            </Button>
          </div>
          <div className="flex items-center gap-3">
            <Circle className="h-5 w-5 text-muted-foreground" />
            <span className="text-sm font-medium">Invite a team member</span>
            <Button variant="link" className="h-auto p-0 ml-auto" asChild>
              <Link href="/settings">Go to Settings</Link>
            </Button>
          </div>
          <div className="flex items-center gap-3 opacity-50">
            <Circle className="h-5 w-5 text-muted-foreground" />
            <span className="text-sm font-medium">Connect your bank (coming soon)</span>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { icon: Receipt, label: 'Record Expense', href: '/expenses', color: 'text-indigo-600', bg: 'bg-indigo-50' },
          { icon: FileText, label: 'New Invoice', href: '/invoices', color: 'text-green-600', bg: 'bg-green-50' },
          { icon: BarChart3, label: 'View Reports', href: '/reports', color: 'text-purple-600', bg: 'bg-purple-50' },
          { icon: Calculator, label: 'Tax Center', href: '/tax', color: 'text-amber-600', bg: 'bg-amber-50' },
        ].map((action, i) => {
          const Icon = action.icon
          return (
            <Link key={i} href={action.href}>
              <Card className="hover:border-primary/50 transition-colors cursor-pointer h-full">
                <CardContent className="p-6 flex flex-col items-center justify-center text-center gap-3">
                  <div className={`p-3 rounded-full ${action.bg}`}>
                    <Icon className={`h-6 w-6 ${action.color}`} />
                  </div>
                  <span className="font-medium text-sm">{action.label}</span>
                </CardContent>
              </Card>
            </Link>
          )
        })}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {[
          { icon: TrendingUp, label: 'Total Revenue' },
          { icon: TrendingDown, label: 'Total Expenses' },
          { icon: Clock, label: 'Outstanding Invoices' },
          { icon: Wallet, label: 'Cash Position' },
        ].map((stat, i) => {
          const Icon = stat.icon
          return (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="flex items-center justify-between space-y-0 pb-2">
                  <p className="text-sm font-medium text-muted-foreground">{stat.label}</p>
                  <Icon className="h-4 w-4 text-muted-foreground" />
                </div>
                <div className="text-2xl font-bold mt-2">—</div>
                <p className="text-xs text-muted-foreground mt-1">No data yet</p>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center h-64 text-center">
            <Activity className="h-12 w-12 text-muted-foreground/30 mb-4" />
            <p className="text-lg font-medium">Your financial activity will appear here</p>
            <p className="text-sm text-muted-foreground max-w-sm mt-2 mb-6">
              Once you start adding expenses and creating invoices, you&apos;ll see a timeline of your business activity.
            </p>
            <Button asChild>
              <Link href="/expenses">Record your first expense</Link>
            </Button>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-primary" />
              AI Insights
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center h-64 text-center">
            <p className="text-sm text-muted-foreground">
              Insights will appear as you add financial data. Taxlot will automatically analyze your spending patterns and alert you to tax saving opportunities.
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  )
}
