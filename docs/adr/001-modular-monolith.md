# ADR 001: Modular Monolith Architecture

Date: 2026-09-13
Status: Accepted

## Context

Taxlot is a financial accounting platform for SMEs. We need to decide on the initial system architecture.

## Decision

We will use a **modular monolith** architecture for the initial system, not microservices.

## Rationale

1. **Simplicity**: A monolith is simpler to develop, test, and deploy in the early stages
2. **Team size**: Small initial team does not justify distributed system overhead
3. **Financial domain**: Accounting operations benefit from ACID transactions within a single database
4. **Iteration speed**: Faster to refactor module boundaries within a monolith than across service boundaries
5. **Operational simplicity**: One deployment unit, one database, one set of logs

## Module Boundaries

Each module owns:
- Domain logic
- Services
- Repository (database access)
- REST API endpoints
- Database migrations (scoped to its domain)

Modules communicate through explicit service interfaces, not direct repository access.

## Service Extraction Policy

A module may only become an independent service if there is a **measurable reason**:
- Independent scaling requirement
- Independent deployment requirement
- Strong domain boundary and separate team ownership
- Infrastructure isolation requirement

Candidates for future extraction: AI Processing, Notifications, Document Processing, Reporting.

The **accounting core** should remain tightly controlled.

## Consequences

- All modules run in one JVM process
- One PostgreSQL database with schema isolation via module-specific prefixes/schemas (considered but deferred)
- Simpler CI/CD pipeline
- Must be disciplined about module boundaries from day one to avoid big-ball-of-mud
