import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Settings</h2>
        <p className="text-muted-foreground">Manage your organization and account preferences.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Organization Profile</CardTitle>
            <CardDescription>Update your business details and contact information.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>Business Name</Label>
              <Input defaultValue="Acme Corp" disabled />
            </div>
            <div className="space-y-2">
              <Label>Tax ID / VAT Number</Label>
              <Input placeholder="Enter Tax ID" disabled />
            </div>
            <Button disabled>Save Changes</Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Team Members</CardTitle>
            <CardDescription>Manage who has access to your organization.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-md border p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">You</p>
                  <p className="text-sm text-muted-foreground">Owner</p>
                </div>
              </div>
            </div>
            <Button variant="outline" className="w-full" disabled>Invite Member</Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
