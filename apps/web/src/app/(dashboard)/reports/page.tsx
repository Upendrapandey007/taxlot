import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { BarChart3 } from 'lucide-react'

export default function ReportsPage() {
  return (
    <div className="flex flex-col h-[calc(100vh-10rem)]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Reports</h2>
          <p className="text-muted-foreground">Gain insights into your business performance.</p>
        </div>
      </div>

      <Card className="flex-1 flex flex-col items-center justify-center text-center">
        <CardContent className="pt-6 flex flex-col items-center justify-center">
          <div className="bg-muted p-6 rounded-full mb-6">
            <BarChart3 className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold mb-2">No data to report</h3>
          <p className="text-muted-foreground max-w-sm mb-6">
            Once you add expenses and invoices, your financial reports will automatically generate here.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
