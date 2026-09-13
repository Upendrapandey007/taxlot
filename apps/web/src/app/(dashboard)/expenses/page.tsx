import Link from 'next/link'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Receipt } from 'lucide-react'

export default function ExpensesPage() {
  return (
    <div className="flex flex-col h-[calc(100vh-10rem)]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Expenses</h2>
          <p className="text-muted-foreground">Manage your business expenses and attachments.</p>
        </div>
        <Button>Record Expense</Button>
      </div>

      <Card className="flex-1 flex flex-col items-center justify-center text-center">
        <CardContent className="pt-6 flex flex-col items-center justify-center">
          <div className="bg-muted p-6 rounded-full mb-6">
            <Receipt className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold mb-2">No expenses recorded</h3>
          <p className="text-muted-foreground max-w-sm mb-6">
            Start tracking your business expenses to keep your finances organized and ready for tax season.
          </p>
          <Button>Record your first expense</Button>
        </CardContent>
      </Card>
    </div>
  )
}
