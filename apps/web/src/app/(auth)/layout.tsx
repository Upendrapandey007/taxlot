import Link from "next/link"

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50/50 p-4">
      <div className="absolute top-4 left-4">
        <Link href="/" className="text-xl font-bold text-primary">
          Taxlot
        </Link>
      </div>
      <div className="w-full max-w-md space-y-6">
        {children}
      </div>
    </div>
  )
}
