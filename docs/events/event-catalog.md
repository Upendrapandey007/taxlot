# Taxlot — Event Catalog

All events use the `taxlot.events` RabbitMQ topic exchange.
Routing key pattern: `<producer>.<aggregate>.<event>`

## Standard Event Envelope

```json
{
  "eventId":       "uuid",
  "eventType":     "UserRegistered",
  "eventVersion":  1,
  "occurredAt":    "2026-01-01T10:00:00Z",
  "producer":      "auth-service",
  "tenantId":      "uuid | null",
  "aggregateType": "credential",
  "aggregateId":   "uuid",
  "traceId":       "uuid",
  "payload":       {}
}
```

---

## Identity Events

### `UserRegistered`
- **Routing key**: `auth.user.registered`
- **Producer**: auth-service
- **Consumers**: user-service
- **Payload**:
```json
{ "userId": "uuid", "email": "string", "fullName": "string", "registeredAt": "datetime" }
```

---

## Tenant Events

### `OrganizationCreated`
- **Routing key**: `organization.organization.created`
- **Producer**: organization-service
- **Consumers**: accounting-service (initializes chart of accounts)
- **Payload**:
```json
{ "organizationId": "uuid", "name": "string", "industry": "string", "currencyCode": "string", "countryCode": "string" }
```

### `MemberInvited`
- **Routing key**: `organization.member.invited`
- **Producer**: organization-service
- **Consumers**: notification-service
- **Payload**:
```json
{ "organizationId": "uuid", "invitedEmail": "string", "role": "string", "invitedBy": "uuid" }
```

---

## Invoice Events

### `InvoiceIssued`
- **Routing key**: `invoice.invoice.issued`
- **Producer**: invoice-service
- **Consumers**: accounting-service, notification-service
- **Payload**:
```json
{
  "invoiceId": "uuid", "organizationId": "uuid", "customerId": "uuid",
  "invoiceNumber": "string", "grandTotal": "string", "currencyCode": "string",
  "issuedAt": "datetime"
}
```

### `InvoiceCancelled`
- **Routing key**: `invoice.invoice.cancelled`
- **Producer**: invoice-service
- **Consumers**: accounting-service

---

## Payment Events

### `PaymentReceived`
- **Routing key**: `payment.payment.received`
- **Producer**: payment-service
- **Consumers**: accounting-service, invoice-service (update status)

### `PaymentRefunded`
- **Routing key**: `payment.payment.refunded`
- **Producer**: payment-service
- **Consumers**: accounting-service

---

## Expense Events

### `ExpenseRecorded`
- **Routing key**: `expense.expense.recorded`
- **Producer**: expense-service
- **Consumers**: accounting-service

### `ExpenseApproved`
- **Routing key**: `expense.expense.approved`
- **Producer**: expense-service
- **Consumers**: notification-service

---

## Accounting Events

### `JournalEntryPosted`
- **Routing key**: `accounting.journal.posted`
- **Producer**: accounting-service
- **Consumers**: reporting-service

### `AccountingPeriodLocked`
- **Routing key**: `accounting.period.locked`
- **Producer**: accounting-service
- **Consumers**: reporting-service, tax-service

---

## Dead-Letter Queues

Every queue has a corresponding dead-letter queue (suffix `.dead`).
Messages move to DLQ after exhausting retry attempts.
DLQ messages must trigger operational alerts.
