import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Users } from 'lucide-react'

export default function CustomersPage() {
  return (
    <div className="flex flex-col h-[calc(100vh-10rem)]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Customers</h2>
          <p className="text-muted-foreground">Manage your client and customer relationships.</p>
        </div>
        <Button>Add Customer</Button>
      </div>

      <Card className="flex-1 flex flex-col items-center justify-center text-center">
        <CardContent className="pt-6 flex flex-col items-center justify-center">
          <div className="bg-muted p-6 rounded-full mb-6">
            <Users className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold mb-2">No customers added</h3>
          <p className="text-muted-foreground max-w-sm mb-6">
            Keep track of your clients' details, billing history, and outstanding balances in one place.
          </p>
          <Button>Add your first customer</Button>
        </CardContent>
      </Card>
    </div>
  )
}
