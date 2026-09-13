"use client"
import { Bell, Search } from "lucide-react"
import { usePathname } from "next/navigation"

export function Header() {
  const pathname = usePathname()
  
  const getPageTitle = () => {
    const path = pathname.split('/')[1]
    if (!path) return 'Dashboard'
    return path.charAt(0).toUpperCase() + path.slice(1)
  }

  return (
    <header className="h-16 border-b bg-background flex items-center justify-between px-6 sticky top-0 z-40">
      <h1 className="text-lg font-semibold">{getPageTitle()}</h1>
      
      <div className="flex items-center space-x-4">
        <button className="text-muted-foreground hover:text-foreground">
          <Search className="h-5 w-5" />
        </button>
        <button className="text-muted-foreground hover:text-foreground relative">
          <Bell className="h-5 w-5" />
          <span className="absolute top-0 right-0 h-2 w-2 rounded-full bg-primary" />
        </button>
      </div>
    </header>
  )
}
