import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FileText } from 'lucide-react'

export default function InvoicesPage() {
  return (
    <div className="flex flex-col h-[calc(100vh-10rem)]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Invoices</h2>
          <p className="text-muted-foreground">Create and manage professional invoices.</p>
        </div>
        <Button>Create Invoice</Button>
      </div>

      <Card className="flex-1 flex flex-col items-center justify-center text-center">
        <CardContent className="pt-6 flex flex-col items-center justify-center">
          <div className="bg-muted p-6 rounded-full mb-6">
            <FileText className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold mb-2">No invoices created</h3>
          <p className="text-muted-foreground max-w-sm mb-6">
            Get paid faster by creating professional invoices and sending them directly to your customers.
          </p>
          <Button>Create your first invoice</Button>
        </CardContent>
      </Card>
    </div>
  )
}
