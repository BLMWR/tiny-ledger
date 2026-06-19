# Tiny Ledger

A minimal REST API for recording money movements, checking balances, and viewing transaction history.

## Tech Stack

| Layer | Choice                                        |
|---|-----------------------------------------------|
| Language | Java 17                                       |
| Framework | Spring Boot 4.0.0 (Spring Framework 7)        |
| Build | Gradle 8.14                    |
| Storage | In-memory (`ConcurrentHashMap` + `ArrayList`) |

## Prerequisites

- **Java 17+** on your `PATH`
- No database or other external software required

## Running the Application

```bash
# Clone and enter the directory
git clone <repo-url>
cd tiny-ledger

# Build and run (downloads Gradle automatically via the wrapper)
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

The server starts on **http://localhost:8080**.

## Running the Tests

```bash
./gradlew test
```

---

## API Reference

A single **default** account (`id = "default"`) is created at startup.  
All endpoints follow the pattern `/accounts/{accountId}/...`.

### Get Balance

```
GET /accounts/{accountId}/balance
```

**Example**

```bash
curl http://localhost:8080/accounts/default/balance
```

```json
{
  "accountId": "default",
  "balance": 0
}
```

---

### Deposit

```
POST /accounts/{accountId}/deposits
Content-Type: application/json

{
  "amount": <positive decimal>,   // required
  "description": "<string>"       // optional
}
```

**Example**

```bash
curl -X POST http://localhost:8080/accounts/default/deposits \
     -H "Content-Type: application/json" \
     -d '{"amount": 1000.00, "description": "salary"}'
```

```json
{
  "id": "3f2e1c4a-...",
  "type": "DEPOSIT",
  "amount": 1000.00,
  "description": "salary",
  "timestamp": "2025-11-20T09:00:00Z",
  "balanceAfter": 1000.00
}
```

---

### Withdraw

```
POST /accounts/{accountId}/withdrawals
Content-Type: application/json

{
  "amount": <positive decimal>,   // required - must not exceed current balance
  "description": "<string>"       // optional
}
```

**Example**

```bash
curl -X POST http://localhost:8080/accounts/default/withdrawals \
     -H "Content-Type: application/json" \
     -d '{"amount": 250.00, "description": "rent"}'
```

```json
{
  "id": "7a8b9c0d-...",
  "type": "WITHDRAWAL",
  "amount": 250.00,
  "description": "rent",
  "timestamp": "2025-11-20T09:01:00Z",
  "balanceAfter": 750.00
}
```

---

### Transaction History

```
GET /accounts/{accountId}/transactions
```

Returns all transactions in chronological order (oldest first).

**Example**

```bash
curl http://localhost:8080/accounts/default/transactions
```

```json
[
  {
    "id": "3f2e1c4a-...",
    "type": "DEPOSIT",
    "amount": 1000.00,
    "description": "salary",
    "timestamp": "2025-11-20T09:00:00Z",
    "balanceAfter": 1000.00
  },
  {
    "id": "7a8b9c0d-...",
    "type": "WITHDRAWAL",
    "amount": 250.00,
    "description": "rent",
    "timestamp": "2025-11-20T09:01:00Z",
    "balanceAfter": 750.00
  }
]
```

---

## Error Responses (RFC 7807 Problem Detail)

| Scenario | HTTP Status |
|---|---|
| Account not found | `404 Not Found` |
| Insufficient funds | `422 Unprocessable Entity` |
| Invalid / missing `amount` | `400 Bad Request` |

---

## Assumptions

1. **Single account at startup.** One `"default"` account is seeded. The URL structure (`/accounts/{accountId}/...`) supports multiple accounts with zero structural changes - they need to be created first (out of scope here).
2. **No negative balances.** Withdrawals that would take the balance below zero are rejected with `422`.
3. **Amounts are always positive.** Direction is expressed by the endpoint (`/deposits` vs `/withdrawals`)
4. **`BigDecimal` for money.** Avoids floating-point rounding errors that `double` would introduce.
5. **No authentication.** Out of scope per the brief.
6. **In-memory only.** Data is lost when the application stops - intentional per the brief.
7. **Thread safety.** Each mutation is `synchronized` on the account object to prevent races (e.g. two concurrent withdrawals both passing the balance check).
8. **Timestamps in UTC.** All `timestamp` fields are ISO-8601 UTC instants.
9. **History is immutable from the outside.** `getTransactions()` returns an unmodifiable view of the list.
