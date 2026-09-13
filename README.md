# Taxlot

> The easiest accounting platform for modern SMEs.

Taxlot is a modern financial management platform built for small and medium businesses. It provides invoicing, expense tracking, tax management, and financial reporting with a clean, approachable interface.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Kotlin + Spring Boot 3.3 + Java 21 |
| Database | PostgreSQL + Flyway + jOOQ |
| Cache | Redis |
| Web | Next.js 14 + React + TypeScript + Tailwind CSS |
| Android | Kotlin + Jetpack Compose |
| Infra | Docker + GitHub Actions + Cloudflare |

---

## Repository Structure

`
taxlot/
+-- apps/
¦   +-- backend/          # Kotlin + Spring Boot API
¦   +-- web/              # Next.js web application
¦   +-- android/          # Kotlin + Jetpack Compose
+-- packages/
¦   +-- shared-contracts/ # OpenAPI spec, shared types
+-- infrastructure/
¦   +-- docker/           # Docker Compose for local dev
¦   +-- terraform/        # IaC (added later)
+-- docs/
¦   +-- adr/              # Architecture Decision Records
+-- .github/
    +-- workflows/        # CI/CD pipelines
`

---

## Getting Started

### Prerequisites

- Docker Desktop
- Java 21 (for backend development)
- Node.js 20+ (for web development)

### 1. Start Local Services

`ash
cd infrastructure/docker
docker compose up -d
`

This starts PostgreSQL, Redis, Mailhog, and MinIO locally.

### 2. Run the Backend

`ash
cd apps/backend
./gradlew bootRun --args='--spring.profiles.active=local'
`

API available at: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Run the Web App

`ash
cd apps/web
npm install
npm run dev
`

Web app available at: http://localhost:3000

---

## Local Dev Services

| Service | URL | Credentials |
|---|---|---|
| PostgreSQL | localhost:5432 | taxlot / taxlot |
| Redis | localhost:6379 | — |
| Mailhog (email UI) | http://localhost:8025 | — |
| MinIO (S3 UI) | http://localhost:9001 | minioadmin / minioadmin |

---

## Environment Variables

Copy .env.example to .env in each app directory:

`ash
cp apps/backend/.env.example apps/backend/.env
cp apps/web/.env.example apps/web/.env.local
`

---

## Architecture

Taxlot uses a **modular monolith** architecture:

- Each module owns its domain logic, services, persistence, and API
- Modules communicate through explicit interfaces
- Financial data is ledger-first with double-entry bookkeeping
- All financial amounts use BigDecimal / NUMERIC — never floating point
- The client is never the source of financial truth

---

## Development

### Running Tests (Backend)

`ash
cd apps/backend
# Unit tests only
./gradlew test

# Integration tests (requires Docker for Testcontainers)
./gradlew integrationTest
`

### Code Style

- Kotlin: follow Kotlin coding conventions
- TypeScript: ESLint + strict mode
- Git: conventional commits (eat:, ix:, chore:, docs:)

---

## License

Proprietary. All rights reserved.
