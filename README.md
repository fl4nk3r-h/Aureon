# Finance Dashboard Backend

A production-structured Spring Boot 3 backend for a finance dashboard system with email OTP authentication, role-based access control, financial record management, and analytics APIs.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL |
| Migrations | Flyway |
| Auth | Email OTP → JWT (stateless) |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build | Maven |

---

## Architecture Overview

```
com.finance.backend
├── config/          # Security, JPA auditing, OpenAPI, app properties
├── controller/      # REST controllers (versioned under /api/v1/)
├── dto/
│   ├── request/     # Validated inbound payloads
│   └── response/    # Outbound response shapes
├── entity/          # JPA entities (User, OtpToken, FinancialRecord)
├── enums/           # Role, UserStatus, TransactionType
├── exception/       # Domain exceptions + global handler
├── repository/      # Spring Data JPA repos + Specification
├── security/        # JWT filter, JwtUtils, AuthenticatedUser principal
├── service/         # Interfaces + implementations
└── util/            # OtpGenerator
```

---

## Authentication Flow

This system uses **passwordless OTP authentication**. There are no passwords stored anywhere.

```
1. POST /api/v1/auth/send-otp      { "email": "user@example.com" }
        ↓
   System looks up the user (must already exist, created by Admin)
   Generates a 6-digit OTP, stores it with a 10-minute expiry
   Sends OTP to the email via Gmail SMTP
        ↓
2. POST /api/v1/auth/verify-otp    { "email": "...", "otp": "123456" }
        ↓
   Validates OTP (checks used=false and expiry)
   Marks OTP as used (one-time use only)
   Returns a signed JWT Bearer token
        ↓
3. All subsequent requests:
   Authorization: Bearer <token>
```

**Key design decisions:**

- OTPs are single-use and invalidated on verification
- Any new OTP request invalidates previous unused OTPs for that email
- Expired OTP tokens are cleaned up nightly via a scheduled job
- JWT contains: email, userId, role — no DB lookup needed per request

---

## Role-Based Access Control

| Endpoint Group | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| GET /dashboard/** | ✓ | ✓ | ✓ |
| GET /records/** | ✗ | ✓ | ✓ |
| POST/PATCH/DELETE /records/** | ✗ | ✗ | ✓ |
| /users/** (all methods) | ✗ | ✗ | ✓ |

Access control is enforced at two levels:

1. **URL-level** in `SecurityConfig` via `authorizeHttpRequests`
2. **Method-level** via `@PreAuthorize` on controllers where additional granularity is needed

---

## API Reference

All APIs are versioned under `/api/v1/`. Full interactive docs available at:

```
http://localhost:8080/swagger-ui.html
```

### Authentication — `/api/v1/auth`

```
POST /api/v1/auth/send-otp
Body: { "email": "user@example.com" }
Response: { "message": "OTP sent to user@example.com. Valid for 10 minutes." }

POST /api/v1/auth/verify-otp
Body: { "email": "user@example.com", "otp": "123456" }
Response: { "token": "<jwt>", "tokenType": "Bearer", "user": { ... } }
```

### Dashboard — `/api/v1/dashboard` *(VIEWER+)*

```
GET /api/v1/dashboard/summary
Response: { totalIncome, totalExpenses, netBalance, totalRecords }

GET /api/v1/dashboard/categories
Response: [{ category, type, total }, ...]

GET /api/v1/dashboard/trends?from=2024-01-01&to=2024-12-31
Response: [{ year, month, type, total }, ...]

GET /api/v1/dashboard/recent?limit=10
Response: [{ id, amount, type, category, recordDate, ... }, ...]
```

### Financial Records — `/api/v1/records` *(GET: ANALYST+, mutate: ADMIN)*

```
GET /api/v1/records
  ?type=INCOME|EXPENSE
  &category=Salary          (partial match)
  &from=2024-01-01
  &to=2024-12-31
  &search=rent              (searches category + notes)
  &page=0&size=20
  &sortBy=recordDate&direction=desc
Response: PagedResponse<RecordResponse>

GET  /api/v1/records/{id}
POST /api/v1/records            Body: { amount, type, category, recordDate, notes }
PATCH /api/v1/records/{id}      Body: (any subset of above fields)
DELETE /api/v1/records/{id}     → soft delete (sets deleted_at)
```

### Users — `/api/v1/users` *(ADMIN only)*

```
GET    /api/v1/users            ?page=0&size=20&sortBy=createdAt&direction=desc
GET    /api/v1/users/{id}
POST   /api/v1/users            Body: { name, email, role }
PATCH  /api/v1/users/{id}       Body: { name?, role?, status? }
DELETE /api/v1/users/{id}       → soft delete
```

---

## Setup & Running Locally

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 14+
- A Gmail account with an [App Password](https://support.google.com/accounts/answer/185833) (2FA must be enabled)

### 1. Create the database

```sql
CREATE DATABASE finance_db;
```

### 2. Configure environment

Copy `.env.example` to `.env` and fill in your values, or export the environment variables directly:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/finance_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_db_password

export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-gmail-app-password   # NOT your regular password

export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
# Generate your own: openssl rand -hex 32
```

### 3. Run

```bash
mvn spring-boot:run
```

Flyway will automatically create all tables and seed the initial admin user on first run.

**Initial admin credentials:**

```
Email: admin@finance.dev
```

Send an OTP to this address to log in as admin, then create other users.

### 4. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize** (top right) and paste your Bearer token after calling `verify-otp`.

---

## Running Tests

```bash
mvn test
```

Tests use **H2 in-memory database** — no PostgreSQL needed to run tests. Flyway is disabled for tests; the schema is created directly by Hibernate.

Test coverage:

- `AuthServiceTest` — OTP send/verify flows, edge cases
- `UserServiceTest` — CRUD, conflict detection, soft delete
- `FinancialRecordServiceTest` — CRUD, partial update, soft delete
- `DashboardServiceTest` — summary aggregation, category mapping, limit clamping

---

## Soft Delete

Both `User` and `FinancialRecord` support soft deletion. Deleted records set a `deleted_at` timestamp and are automatically excluded from all queries via Hibernate's `@SQLRestriction("deleted_at IS NULL")`. This means:

- No data is ever physically removed
- All standard `findAll`, `findById`, `count()` calls automatically exclude deleted rows
- Deleted users cannot authenticate (the JWT filter checks `user.isActive()`)

---

## Design Decisions & Assumptions

| Decision | Rationale |
|---|---|
| OTP-only auth (no passwords) | Simpler, no password hashing infrastructure needed, appropriate for internal dashboards |
| JWT is stateless | No session store required; role is embedded in the token |
| Users must be pre-created by admin | Prevents self-registration; finance dashboards are typically invite-only systems |
| Soft delete on all primary entities | Preserves audit trail; financial records must never be truly destroyed |
| `@SQLRestriction` for soft delete | Cleaner than adding `WHERE deleted_at IS NULL` to every query manually |
| Specification pattern for record filtering | Composable, type-safe dynamic queries without query string building |
| `PATCH` instead of `PUT` for updates | Allows partial updates without requiring all fields to be re-sent |
| OTP cleanup via scheduled job | Prevents OTP table from growing unboundedly in production |

---

## Project Structure Summary

```
src/
├── main/
│   ├── java/com/finance/backend/
│   │   ├── config/           AppProperties, JpaConfig, OpenApiConfig, SecurityConfig
│   │   ├── controller/       AuthController, UserController, FinancialRecordController, DashboardController
│   │   ├── dto/              Request and Response records
│   │   ├── entity/           User, OtpToken, FinancialRecord
│   │   ├── enums/            Role, UserStatus, TransactionType
│   │   ├── exception/        GlobalExceptionHandler + domain exceptions
│   │   ├── repository/       JPA repositories + FinancialRecordSpecification
│   │   ├── security/         JwtUtils, JwtAuthFilter, AuthenticatedUser
│   │   ├── service/          Interfaces + implementations
│   │   └── util/             OtpGenerator
│   └── resources/
│       ├── application.yml
│       └── db/migration/     V1__initial_schema.sql, V2__seed_admin.sql
└── test/
    ├── java/com/finance/backend/service/   Unit tests
    └── resources/application.yml          H2 test config
```
