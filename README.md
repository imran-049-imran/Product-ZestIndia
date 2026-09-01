# Product API

A RESTful CRUD API for Products (and their Items) built with **Java 17** and **Spring Boot 3**, secured with **JWT + refresh token rotation**, backed by **PostgreSQL**, documented with **Swagger/OpenAPI**, and fully containerized with **Docker**.

Built for the Zest India IT Services Java Backend Developer assignment.

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Data JPA (Hibernate) + MySQL (H2 for tests)
- Spring Security 6 with stateless JWT auth + refresh token rotation
- Jakarta Bean Validation
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + Spring Boot Test
- Docker & Docker Compose

## Architecture

```
com.zestindia.productapi
├── config          # Security, OpenAPI, JPA auditing, async, CORS
├── security         # JWT util, auth filter, UserDetailsService
├── entity           # JPA entities: Product, Item, AppUser, RefreshToken
├── repository       # Spring Data JPA repositories
├── dto              # Request/response payloads (decoupled from entities)
├── service           # Interfaces + impl (business logic, transactional boundaries)
├── controller       # REST controllers (thin, delegate to services)
└── exception        # Custom exceptions + @RestControllerAdvice global handler
```

**Layering:** Controller → Service → Repository. Controllers never touch entities directly — DTOs are used at the API boundary to keep the persistence model independent of the wire format. `BaseAuditEntity` centralizes `created_by/on` and `modified_by/on`, populated automatically via Spring Data JPA auditing (the "auditor" resolves to the authenticated username, or `system` for unauthenticated calls).

**Security model:** Access tokens are short-lived JWTs (15 min) validated per-request by a custom `OncePerRequestFilter`. Refresh tokens are opaque UUIDs persisted server-side, allowing revocation; every `/auth/refresh` call **rotates** the token — the old one is marked revoked and a new pair is issued, limiting the blast radius of a leaked refresh token.

**Error handling:** A single `GlobalExceptionHandler` maps domain exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, validation errors, auth failures) to a consistent `ErrorResponse` JSON shape with timestamp, status, message, path, and field-level details where relevant.

**Pagination:** All collection endpoints (`GET /products`, `GET /products/{id}/items`) accept `page`, `size`, `sortBy`, `direction` and return a uniform `PagedResponse<T>` envelope.

## API Endpoints

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user | Public |
| POST | `/api/v1/auth/login` | Login, get access + refresh token | Public |
| POST | `/api/v1/auth/refresh` | Rotate refresh token, get new pair | Public |
| POST | `/api/v1/auth/logout` | Revoke a refresh token | Public |
| GET | `/api/v1/products` | List products (paginated, `?name=` filter) | JWT |
| GET | `/api/v1/products/{id}` | Get product by id | JWT |
| POST | `/api/v1/products` | Create product | JWT |
| PUT | `/api/v1/products/{id}` | Update product | JWT |
| DELETE | `/api/v1/products/{id}` | Delete product | JWT |
| GET | `/api/v1/products/{id}/items` | List items for a product (paginated) | JWT |
| POST | `/api/v1/products/{id}/items` | Add an item to a product | JWT |

Swagger UI: `http://localhost:8081/swagger-ui.html`

## Running Locally

### Option 1 — Docker Compose (recommended)

```bash
docker compose up --build
```

This starts MySQL and the API together. The app is available at `http://localhost:8080`.

### Option 2 — Local Maven + local MySQL

1. Start a MySQL instance (the app auto-creates the `productdb` schema on first connect).
2. Set environment variables (or edit `application.yml` defaults):
   ```bash
   export DB_HOST=localhost DB_PORT=3306 DB_NAME=productdb DB_USER=root DB_PASSWORD=root@123
   ```
3. Run:
   ```bash
   mvn spring-boot:run
   ```

## Example Usage

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'
# -> { "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }

# Create a product
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"productName":"Laptop"}'

# Refresh tokens
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

## Running Tests

```bash
mvn test
```

Tests run against an in-memory H2 database (`application-test.yml`, `spring.profiles.active=test`) — no external database required. Includes:
- Unit tests for `ProductServiceImpl` (Mockito-mocked repository)
- Integration tests for `ProductController` (full Spring context + MockMvc, exercising auth, validation, and CRUD)

## Notes on Requirement Coverage

- **Indexing:** `idx_product_name` on `product.product_name`, `idx_item_product_id` on `item.product_id`.
- **Async:** a dedicated `taskExecutor` bean is configured (`AsyncConfig`) and ready for `@Async`-annotated use cases as the domain grows.
- **CORS / HTTPS:** CORS is configured in `SecurityConfig`; HTTPS termination is expected at the reverse proxy/load-balancer layer in deployment (standard practice for containerized Spring Boot services).
