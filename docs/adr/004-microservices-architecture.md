# ADR 004: Microservices Architecture

Date: 2026-09-13
Status: Accepted (supersedes ADR 001)

## Context

ADR 001 selected a modular monolith. After further product and technical review, the Backend Master PRD v1.0 mandates microservices architecture from day one due to:
- Independent business capability ownership requirements
- Independent deployment per service
- Independent scaling requirements
- Isolated data ownership per service
- Future team structure alignment

## Decision

Taxlot will use a **microservices architecture** with 16 independent services plus a shared platform library.

## Service Map

| Service | Port | Database | Responsibility |
|---|---|---|---|
| api-gateway | 8080 | — | Routing, JWT validation, rate limiting |
| auth-service | 8081 | taxlot_auth | Credentials, sessions, tokens |
| user-service | 8082 | taxlot_user | User profiles, preferences |
| organization-service | 8083 | taxlot_organization | Orgs, members, roles, permissions |
| accounting-service | 8084 | taxlot_accounting | Journal, ledger, accounts, periods |
| customer-service | 8085 | taxlot_customer | Customer records |
| invoice-service | 8086 | taxlot_invoice | Invoices, line items, numbering |
| payment-service | 8087 | taxlot_payment | Payments, allocations, refunds |
| expense-service | 8088 | taxlot_expense | Expenses, receipts |
| tax-service | 8089 | taxlot_tax | Tax rules, rates, periods |
| reporting-service | 8090 | taxlot_reporting | P&L, Balance Sheet, Cash Flow |
| document-service | 8091 | taxlot_document | File metadata, S3 integration |
| notification-service | 8092 | taxlot_notification | Email, push, in-app |
| ai-service | 8093 | taxlot_ai | OCR, categorization, insights |
| search-service | 8094 | taxlot_search | Full-text search index |
| integration-service | 8095 | taxlot_integration | External APIs, imports |

## Non-Negotiable Rules

1. No service reads another service's database directly
2. Cross-service communication via REST (sync) or RabbitMQ (async)
3. Critical state-changing events use Transactional Outbox
4. Consumers must be idempotent (handle duplicates safely)
5. Accounting service is the financial source of truth
6. Each service owns its Flyway migrations

## Communication Patterns

- **Synchronous**: REST via API Gateway for client-facing operations
- **Asynchronous**: RabbitMQ topic exchange `taxlot.events`
  - Routing key pattern: `<producer-service>.<aggregate-type>.<event-type>`
  - Example: `auth.user.registered`, `invoice.invoice.issued`

## Consequences

- Higher operational complexity than monolith
- Mitigated by: monorepo, Docker Compose local dev, Gradle multi-project build
- Clear service boundaries enforced from the start prevent big-ball-of-mud
- Each service independently deployable and testable
