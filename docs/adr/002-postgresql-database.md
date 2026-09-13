# ADR 002: PostgreSQL as Primary Database

Date: 2026-09-13
Status: Accepted

## Context

We need a database for Taxlot's financial data.

## Decision

Use **PostgreSQL** as the primary and only database.

## Rationale

1. **ACID transactions**: Essential for financial data integrity
2. **Relational integrity**: Foreign keys and constraints enforce business rules at the DB layer
3. **NUMERIC type**: PostgreSQL NUMERIC/DECIMAL avoids floating-point precision issues for money
4. **Reporting**: Complex SQL joins for financial reporting (P&L, Balance Sheet)
5. **Mature ecosystem**: Excellent tooling, Flyway migrations, jOOQ support
6. **JSON support**: Can handle flexible data when needed without a separate document store

## Rejected Alternatives

- **MongoDB**: No ACID transactions across documents, not suited for double-entry bookkeeping
- **MySQL**: Less powerful for complex reporting queries, no partial indexes
- **SQLite**: Not suitable for production multi-user access

## Consequences

- All financial state is in PostgreSQL
- Redis is supporting infrastructure (cache, rate limiting) — never the source of truth
- Flyway manages all schema changes
- Migrations are committed to Git and tested before deployment
