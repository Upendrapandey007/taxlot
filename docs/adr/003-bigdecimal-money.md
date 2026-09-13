# ADR 003: BigDecimal for All Financial Amounts

Date: 2026-09-13
Status: Accepted

## Context

Financial calculations require exact decimal arithmetic. IEEE 754 floating-point types (Double, Float) cannot represent many decimal fractions exactly.

## Decision

All financial amounts in Taxlot will use:
- **Kotlin**: java.math.BigDecimal
- **PostgreSQL**: NUMERIC(19,4) (19 digits total, 4 decimal places)

## Rationale

Floating-point arithmetic can produce errors like:
`
0.1 + 0.2 = 0.30000000000000004
`

In a financial system, this is unacceptable.

NUMERIC(19,4) provides:
- Up to 999,999,999,999,999 (15 digits) before decimal
- 4 decimal places (sufficient for most currencies)
- Exact representation

## Consequences

- No Double, Float, or loat types for any monetary value
- Code reviews must reject floating-point money types
- All arithmetic uses BigDecimal methods (not operators that might auto-convert)
- Rounding must be explicit (use RoundingMode.HALF_UP for financial rounding)
