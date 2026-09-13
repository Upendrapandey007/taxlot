import { Sidebar } from "@/components/layout/sidebar"
import { MobileNav } from "@/components/layout/mobile-nav"
import { Header } from "@/components/layout/header"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="flex min-h-screen bg-gray-50/30">
      <Sidebar />
      <div className="flex-1 md:pl-60 pb-16 md:pb-0">
        <Header />
        <main className="p-6 max-w-7xl mx-auto space-y-6">
          {children}
        </main>
      </div>
      <MobileNav />
    </div>
  )
}
