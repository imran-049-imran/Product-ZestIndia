# Product API

A production-style RESTful CRUD API for Products and their Items, built using **Java 21** and **Spring Boot 3.5.4**.

Built for the Zest India IT Services Java Backend Developer Technical Assessment.

## Key Features

- Product CRUD operations
- Product Items management
- RESTful API design using `/api/v1`
- JWT-based authentication
- Refresh token with rotation
- Role-based authorization
- Request validation using Jakarta Bean Validation
- Global exception handling with standardized error responses
- Pagination and sorting
- Database indexing
- JPA/Hibernate persistence
- Swagger/OpenAPI documentation
- Unit testing with JUnit 5 and Mockito
- Integration testing with Spring Boot Test (H2 in-memory database)
- Docker and Docker Compose support
- CORS configuration
- Async processing configuration
- Audit fields for created/modified information

---

## Tech Stack

| Technology          | Version / Usage                |
|----------------------|--------------------------------|
| Java                 | 21                              |
| Spring Boot          | 3.5.4                           |
| Spring Web           | REST APIs                       |
| Spring Data JPA      | Persistence                     |
| Hibernate            | ORM                             |
| MySQL                | Production database             |
| H2                    | Testing database                |
| Spring Security       | Authentication & authorization  |
| JWT                    | Access token authentication     |
| Jakarta Validation      | Request validation              |
| JUnit 5                  | Unit testing                    |
| Mockito                   | Mocking                         |
| Spring Boot Test            | Integration testing             |
| Swagger / OpenAPI             | API documentation                |
| Maven                          | Build tool                       |
| Docker                          | Containerization                 |
| Docker Compose                    | Multi-container deployment       |
| Git / GitHub                        | Version control                  |

---

## Architecture

The application follows a layered architecture to maintain separation of concerns, readability, and maintainability.

```
Client
   │
   ▼
Controller Layer
   │
   ▼
Service Layer
   │
   ▼
Repository Layer
   │
   ▼
MySQL Database
```

### Project Structure

```
src/main/java/com/zestindia/productapi
│
├── config
│   ├── SecurityConfig
│   ├── OpenApiConfig
│   ├── JpaAuditingConfig
│   └── AsyncConfig
│
├── security
│   ├── JwtAuthenticationFilter
│   ├── JwtUtil
│   └── CustomUserDetailsService
│
├── controller
│   ├── AuthController
│   ├── ProductController
│   └── ItemController
│
├── service
│   ├── AuthService
│   ├── ProductService
│   └── ItemService
│
├── repository
│   ├── UserRepository
│   ├── ProductRepository
│   ├── ItemRepository
│   └── RefreshTokenRepository
│
├── entity
│   ├── AppUser
│   ├── Product
│   ├── Item
│   └── RefreshToken
│
├── dto
│   ├── AuthRequest
│   ├── AuthResponse
│   ├── ProductRequest
│   ├── ProductResponse
│   └── ItemResponse
│
└── exception
    ├── GlobalExceptionHandler
    ├── ResourceNotFoundException
    └── ErrorResponse
```

---

## Authentication & Authorization

The application uses Spring Security with JWT authentication.

### Authentication Flow

```
User
 │
 │ Login
 ▼
Authentication API
 │
 │ Valid credentials
 ▼
Access Token + Refresh Token
 │
 ▼
Client
 │
 │ Bearer access token
 ▼
JWT Authentication Filter
 │
 ▼
Protected API
```

**Access Token** — used to authenticate requests to protected APIs.

```
Authorization: Bearer <access-token>
```

**Refresh Token** — persisted server-side and rotated when a new access token is generated, reducing the risk associated with long-lived refresh tokens.

**Role-Based Authorization** — controls access to protected operations based on user roles.

---

## API Reference

**Base URL:** `http://localhost:8081/api/v1`

### Authentication

#### Register User
`POST /api/v1/auth/register`

```json
{
  "username": "admin",
  "password": "admin123",
  "role": "ADMIN"
}
```

#### Login
`POST /api/v1/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response:

```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<refresh-token>"
}
```

#### Refresh Token
`POST /api/v1/auth/refresh`

```json
{
  "refreshToken": "<refresh-token>"
}
```

#### Logout
`POST /api/v1/auth/logout`

### Products

#### Create Product
`POST /api/v1/products`

```json
{
  "productName": "Laptop"
}
```

#### Get All Products
`GET /api/v1/products`

Supports pagination and sorting:

```
GET /api/v1/products?page=0&size=10&sort=productName,asc
```

#### Get Product By ID
`GET /api/v1/products/{id}`

```
GET /api/v1/products/1
```

#### Update Product
`PUT /api/v1/products/{id}`

```json
{
  "productName": "Gaming Laptop"
}
```

#### Delete Product
`DELETE /api/v1/products/{id}`

#### Get Product Items
`GET /api/v1/products/{id}/items`

Returns all items associated with the specified product.

#### Add Product Item
`POST /api/v1/products/{id}/items`

```json
{
  "quantity": 10
}
```

---

## API Summary

| Method | Endpoint                     | Description            |
|--------|-------------------------------|-------------------------|
| POST   | `/api/v1/auth/register`       | Register user           |
| POST   | `/api/v1/auth/login`          | Login                   |
| POST   | `/api/v1/auth/refresh`        | Refresh access token    |
| POST   | `/api/v1/auth/logout`         | Logout                  |
| GET    | `/api/v1/products`            | Get products             |
| GET    | `/api/v1/products/{id}`       | Get product               |
| POST   | `/api/v1/products`            | Create product             |
| PUT    | `/api/v1/products/{id}`       | Update product               |
| DELETE | `/api/v1/products/{id}`       | Delete product                 |
| GET    | `/api/v1/products/{id}/items` | Get product items                |
| POST   | `/api/v1/products/{id}/items` | Add product item                  |

---

## Database Design

The application uses MySQL as the production database.

**`product` table**

```
id              INT PK
product_name    VARCHAR(255)
created_by      VARCHAR(100)
created_on      TIMESTAMP
modified_by     VARCHAR(100)
modified_on     TIMESTAMP
```

**`item` table**

```
id              INT PK
product_id      INT FK
quantity        INT
```

**Relationship:** A `Product` can have multiple `Item`s (one-to-many).

### Indexing

| Table   | Indexed Column | Purpose                                       |
|---------|-----------------|------------------------------------------------|
| product | `product_name`  | Faster lookups/sorting by name                  |
| item    | `product_id`    | Faster retrieval of items belonging to a product |

---

## Validation

Request validation is implemented using Jakarta Bean Validation, including:

- Required fields
- String length validation
- Quantity validation
- Invalid request handling

Invalid requests return a standardized error response instead of exposing internal exceptions.

---

## Global Exception Handling

Centralized exception handling is implemented via `@RestControllerAdvice`, covering:

- Resource not found
- Invalid request
- Validation failure
- Authentication failure
- Authorization failure
- Database-related errors

**Example error response:**

```json
{
  "timestamp": "2026-09-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/api/v1/products/99"
}
```

---

## API Documentation

Swagger/OpenAPI is integrated for interactive API documentation.

After starting the application, open:

```
http://localhost:8081/swagger-ui/index.html
```

Swagger can be used to view available APIs, inspect request/response models, test endpoints, and provide authentication tokens.

---

## Testing

Implemented using JUnit 5, Mockito, Spring Boot Test, MockMvc, and an H2 in-memory database.

**Coverage:** Controller → Service → Repository layers, including:

- Product creation, retrieval, update, deletion
- Product not found scenarios
- Validation errors
- Authentication scenarios
- Controller API responses

Run tests with:

```
mvn test
```

---

## Docker

The application is containerized using Docker. Docker Compose runs:

```
                 Docker Compose
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
        Spring Boot API       MySQL
          Port 8081           Port 3306
```

### Prerequisites

Install Docker and Docker Compose, then verify:

```
docker --version
docker compose version
```

### Start Application

From the project root:

```
docker compose up --build
```

- API: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui/index.html`

### Stop Application

```
docker compose down
```

To also remove the database volume ( deletes local MySQL data):

```
docker compose down -v
```

---

## Running Locally Without Docker

**1. Start MySQL**

```sql
CREATE DATABASE productdb;
```

**2. Configure environment variables**

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=productdb
DB_USER=root
DB_PASSWORD=root@123
```

**3. Build the project**

```
mvn clean install
```

**4. Run the application**

```
mvn spring-boot:run
```

Application available at `http://localhost:8081`.

---

## Configuration

Application configuration is externalized using environment variables where applicable, avoiding hard-coded environment-specific database configuration:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

---

## Security Considerations

- JWT-based authentication
- Refresh token rotation
- Role-based authorization
- Password hashing
- Stateless authentication
- Request validation
- CORS configuration
- Centralized exception handling
- Environment-based configuration

> For production deployment, HTTPS should be enforced at the application or reverse-proxy/load-balancer layer.

---

## Performance Considerations

- Database indexing
- Pagination for collection APIs
- Efficient JPA repository queries
- Separation of controller/service/repository responsibilities
- Async configuration where applicable

Pagination prevents large datasets from being returned in a single request.

---

## Example API Flow

1. Register
2. Login
3. Receive JWT access token
4. Authorize API request
5. Create product
6. Retrieve product
7. Update product
8. Retrieve product items
9. Delete product
10. Logout

---

## Repository Structure

```
Product-ZestIndia/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
└── .gitignore
```

---

## Design Principles

- Separation of concerns
- Clean layered architecture
- DTO-based API contracts
- Dependency injection
- Reusable services
- Centralized exception handling
- Secure authentication
- Input validation
- Maintainable code structure
- Testability
- Containerized deployment

---

## Assessment Scope

This project was implemented to demonstrate practical knowledge of:

Java, Spring Boot, REST API development, Spring Data JPA, Hibernate, MySQL, Spring Security, JWT authentication, refresh token management, role-based authorization, validation, exception handling, unit testing, integration testing, Swagger/OpenAPI, Docker, Docker Compose, and Git/GitHub.

---

## Author

**Imran Attar**
Java Developer | Spring Boot | REST APIs | MySQL

GitHub: [github.com/imran-049-imran/Product-ZestIndia](https://github.com/imran-049-imran/Product-ZestIndia)

---

## Disclaimer

This project was developed as a technical assessment submission and is intended to demonstrate backend development, API design, security, testing, and deployment practices.
