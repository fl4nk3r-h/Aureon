Here are all the curl commands to test every endpoint, in the correct order.

---

## Step 1 — Auth (get your token first)

**Request OTP:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@finance.dev"}'
```

**Verify OTP (replace 123456 with the code from your email):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@finance.dev", "otp": "123456"}'
```

Copy the `token` value from the response. Set it as a variable so you don't paste it every time:
```bash
TOKEN="eyJhbGci..."   # paste your token here
```

---

## Step 2 — User Management (ADMIN only)

**Create a VIEWER user:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "John Viewer",
    "email": "john@company.com",
    "role": "VIEWER"
  }'
```

**Create an ANALYST user:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Jane Analyst",
    "email": "jane@company.com",
    "role": "ANALYST"
  }'
```

**List all users (paginated):**
```bash
curl http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN"
```

**List with pagination and sorting:**
```bash
curl "http://localhost:8080/api/v1/users?page=0&size=10&sortBy=createdAt&direction=desc" \
  -H "Authorization: Bearer $TOKEN"
```

**Get user by ID:**
```bash
curl http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Update user role and status:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "role": "ANALYST",
    "status": "INACTIVE"
  }'
```

**Soft-delete a user:**
```bash
curl -X DELETE http://localhost:8080/api/v1/users/2 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Step 3 — Financial Records (ADMIN to write, ANALYST+ to read)

**Create an INCOME record:**
```bash
curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 85000.00,
    "type": "INCOME",
    "category": "Salary",
    "recordDate": "2024-03-01",
    "notes": "March salary"
  }'
```

**Create an EXPENSE record:**
```bash
curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 25000.00,
    "type": "EXPENSE",
    "category": "Office Rent",
    "recordDate": "2024-03-01",
    "notes": "Q1 rent payment"
  }'
```

**Create a few more to make the dashboard interesting:**
```bash
curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount": 12000.00, "type": "INCOME",  "category": "Consulting", "recordDate": "2024-03-15", "notes": "Client project"}'

curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount": 3500.00,  "type": "EXPENSE", "category": "Software",   "recordDate": "2024-03-10", "notes": "SaaS subscriptions"}'

curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount": 1200.00,  "type": "EXPENSE", "category": "Travel",     "recordDate": "2024-03-20", "notes": "Client visit"}'
```

**List all records:**
```bash
curl http://localhost:8080/api/v1/records \
  -H "Authorization: Bearer $TOKEN"
```

**Filter by type:**
```bash
curl "http://localhost:8080/api/v1/records?type=EXPENSE" \
  -H "Authorization: Bearer $TOKEN"
```

**Filter by category (partial match):**
```bash
curl "http://localhost:8080/api/v1/records?category=rent" \
  -H "Authorization: Bearer $TOKEN"
```

**Filter by date range:**
```bash
curl "http://localhost:8080/api/v1/records?from=2024-03-01&to=2024-03-31" \
  -H "Authorization: Bearer $TOKEN"
```

**Search across category and notes:**
```bash
curl "http://localhost:8080/api/v1/records?search=client" \
  -H "Authorization: Bearer $TOKEN"
```

**Combine multiple filters:**
```bash
curl "http://localhost:8080/api/v1/records?type=EXPENSE&from=2024-03-01&to=2024-03-31&page=0&size=5&sortBy=amount&direction=desc" \
  -H "Authorization: Bearer $TOKEN"
```

**Get a single record by ID:**
```bash
curl http://localhost:8080/api/v1/records/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Partially update a record:**
```bash
curl -X PATCH http://localhost:8080/api/v1/records/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 90000.00,
    "notes": "March salary - revised"
  }'
```

**Soft-delete a record:**
```bash
curl -X DELETE http://localhost:8080/api/v1/records/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Step 4 — Dashboard (VIEWER, ANALYST, ADMIN)

**Overall summary:**
```bash
curl http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"
```

**Category-wise totals:**
```bash
curl http://localhost:8080/api/v1/dashboard/categories \
  -H "Authorization: Bearer $TOKEN"
```

**Monthly trends (last 12 months — default):**
```bash
curl http://localhost:8080/api/v1/dashboard/trends \
  -H "Authorization: Bearer $TOKEN"
```

**Monthly trends for a specific range:**
```bash
curl "http://localhost:8080/api/v1/dashboard/trends?from=2024-01-01&to=2024-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

**Recent activity (last 10):**
```bash
curl http://localhost:8080/api/v1/dashboard/recent \
  -H "Authorization: Bearer $TOKEN"
```

**Recent activity with custom limit:**
```bash
curl "http://localhost:8080/api/v1/dashboard/recent?limit=25" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Step 5 — Test Access Control (should fail)

**VIEWER tries to read records — expect 403:**
```bash
# First get a VIEWER token (send OTP to john@company.com, verify it, set VIEWER_TOKEN)
VIEWER_TOKEN="eyJhbGci..."

curl http://localhost:8080/api/v1/records \
  -H "Authorization: Bearer $VIEWER_TOKEN"
# → 403 Forbidden
```

**ANALYST tries to create a record — expect 403:**
```bash
ANALYST_TOKEN="eyJhbGci..."

curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ANALYST_TOKEN" \
  -d '{"amount": 100, "type": "INCOME", "category": "Test", "recordDate": "2024-03-01"}'
# → 403 Forbidden
```

**No token at all — expect 403:**
```bash
curl http://localhost:8080/api/v1/dashboard/summary
# → 403 Forbidden
```

**Wrong OTP — expect 401:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@finance.dev", "otp": "000000"}'
# → 401 Unauthorized
```

**Unknown email — expect 404:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "nobody@nowhere.com"}'
# → 404 Not Found
```

---

## Step 6 — Test Validation Errors (should return 400)

**Missing required fields:**
```bash
curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"type": "INCOME"}'
# → 400 with errors: { "amount": "required", "category": "required", "recordDate": "required" }
```

**Negative amount:**
```bash
curl -X POST http://localhost:8080/api/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount": -100, "type": "INCOME", "category": "Test", "recordDate": "2024-03-01"}'
# → 400 with errors: { "amount": "Amount must be greater than 0" }
```

**Invalid email format:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "not-an-email"}'
# → 400 with errors: { "email": "Invalid email format" }
```

**Duplicate user email:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "Duplicate", "email": "admin@finance.dev", "role": "VIEWER"}'
# → 409 Conflict
```

---

## Pretty-print JSON (add to any command)

If you have `jq` installed:
```bash
curl http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN" | jq
```

If not, pipe through Python:
```bash
curl http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```