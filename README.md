# Order Service

Order Management service with SAGA orchestration pattern.

## Port
8082

## Features
- Order listing & processing
- Orchestrator SAGA pattern
- Resilience4j retry mechanism (5 attempts with exponential backoff)
- Order status flow: PENDING → FAILED (during retries) → COMPLETED/REVERSAL
- OAuth2 JWT token validation
- Virtual Threads enabled
- Caffeine Caching

## Tech Stack
- Spring Boot 3.5
- Java 21
- PostgreSQL
- Liquibase
- Resilience4j
- Spring Security OAuth2

## Database
```sql
CREATE DATABASE order_db;
```

## Running
```bash
mvn spring-boot:run
```

## Testing
```bash
mvn test
```

## API Endpoints
- `GET /api/orders` - List all orders (cached, requires USER/ADMIN role)
- `POST /api/orders/process` - Process order (requires USER/ADMIN role)

## Order Processing Flow
1. Create order with status PENDING
2. SAGA Orchestrator calls Payment Service with retry
3. Status changes to FAILED during retries
4. On success: status → COMPLETED
5. On failure after 5 retries: status → REVERSAL
