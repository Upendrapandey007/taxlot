import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Calculator } from 'lucide-react'

export default function TaxPage() {
  return (
    <div className="flex flex-col h-[calc(100vh-10rem)]">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Tax Center</h2>
          <p className="text-muted-foreground">Estimated taxes, filings, and deductions.</p>
        </div>
        <Button variant="outline">Tax Settings</Button>
      </div>

      <Card className="flex-1 flex flex-col items-center justify-center text-center">
        <CardContent className="pt-6 flex flex-col items-center justify-center">
          <div className="bg-muted p-6 rounded-full mb-6">
            <Calculator className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold mb-2">Tax estimates are compiling</h3>
          <p className="text-muted-foreground max-w-sm mb-6">
            Taxlot calculates your estimated tax liability based on your recorded income and expenses. Add data to see your estimates.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
