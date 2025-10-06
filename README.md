# Product Service

## Table of Contents
- [Overview](#product-service)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure-excerpt)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start-local-development)
  - [Clone](#1-clone)
  - [Start PostgreSQL](#2-start-postgresql-via-docker-compose)
  - [Run Application](#3-run-the-application)
  - [Verify Health](#4-verify-health--basic-call)
- [Configuration](#configuration)
- [Actuator & Observability](#actuator--observability)
- [Database & Migrations](#database--migrations)
- [Caching](#caching)
- [External Integration](#external-integration-hnb-exchange-rate-api)
- [API Documentation](#api-documentation)
- [REST Endpoints](#rest-endpoints)
- [Product Data Model](#product-data-model-simplified)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Logging](#logging)
- [Common Maven Commands](#common-maven-commands)
- [Deployment](#deployment-outline)
- [Future Improvements](#future-improvements)
- [License](#license)
- [Maintainer](#maintainer)

A Spring Boot REST API for managing products stored in PostgreSQL with EUR pricing automatically
converted to USD via an external exchange rate API (Croatian National Bank).
Database schema managed with Liquibase. OpenAPI (Swagger UI) included.

## Features
- Create and retrieve products
- Pagination support for product listing
- Input validation (Jakarta Validation)
- Automatic EUR to USD price conversion via external HNB API (cached)
- Liquibase database migrations (idempotent & versioned)
- OpenAPI 3 documentation (Swagger UI)
- Centralized exception handling with ProblemDetail
- MapStruct DTO mapping & Lombok boilerplate reduction
- Operational observability via Spring Boot Actuator (health, metrics, info)
- Liveness & readiness probes enabled (Kubernetes friendly)
- Profiles: default (dev) + test (H2 in–memory)

## Tech Stack
- Java 21, Spring Boot 3.5.6
- Spring Web, Spring Data JPA, Spring Cache
- PostgreSQL (runtime), H2 (tests)
- Liquibase for migrations
- MapStruct, Lombok
- springdoc-openapi (Swagger)
- Spring Boot Actuator

## Project Structure (excerpt)
```
src/main/java/org/tech/product_service/
  api/              # Public API contracts (interfaces + Swagger annotations)
  controller/       # REST controllers
  service/          # Business logic (and impl)
  repository/       # Spring Data JPA repositories
  model/            # JPA entities
  dto/              # Request / Response DTOs
  mapper/           # MapStruct mappers
  exception/        # Global exception handling
  external/         # Exchange rate integration
  config/           # Configuration beans (OpenAPI, cache, data, etc.)
resources/
  application.properties  # Default runtime configuration
  db/changelog/           # Liquibase changelogs
```

## Prerequisites
- Java 21 (verify with: `java -version`)
- Maven 3.9+ (wrapper included: `./mvnw`)
- Docker & Docker Compose (for PostgreSQL)
- cURL, HTTP client (e.g., Postman) or Swagger UI for testing

## Quick Start (Local Development)
### 1. Clone
```shell
git clone https://github.com/begicjosip/product-service.git
cd product-service
```

### 2. Start PostgreSQL via Docker Compose
```shell
docker compose up -d
```
This starts a PostgreSQL 17 container with:
- DB: `product_db`
- User: `product_user`
- Password: `product_pass`
- Port: `5432`

To view logs:
```shell
docker logs -f product-service-db
```
To stop:
```shell
docker compose down
```
(Use `docker compose down -v` to also remove the volume.)

### 3. Run the Application
Option A (recommended during dev):
```shell
./mvnw spring-boot:run
```
Option B (package + run):
```shell
./mvnw clean package
java -jar target/product-service-0.0.1.jar
```

App will start on: http://localhost:8080

### 4. Verify Health / Basic Call
Check Actuator health (root, liveness & readiness):
```shell
curl -s http://localhost:8080/actuator/health | jq '.'
curl -s http://localhost:8080/actuator/health/liveness | jq '.'
curl -s http://localhost:8080/actuator/health/readiness | jq '.'
```
List a metric (example):
```shell
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.'
```
List products (functional check):
```shell
curl -s http://localhost:8080/product | jq '.'
```

## Configuration
Main file: `src/main/resources/application.properties`
```
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=product_user
spring.datasource.password=product_pass
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
hnb.api.tecaj.v3.url=https://api.hnb.hr/tecajn-eur/v3
```
Override any property via environment variable (Spring relaxed binding). Examples:
```
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/another_db
export HNB_API_TECAJ_V3_URL=https://api.hnb.hr/tecajn-eur/v3
./mvnw spring-boot:run
```
Or via JVM args:
```
java -jar target/product-service-0.0.1.jar \
  --spring.datasource.password=secret \
  --logging.level.org.tech.product_service=DEBUG
```

## Actuator & Observability
Actuator endpoints exposed (via `management.endpoints.web.exposure.include=health,info,metrics`):
- GET `/actuator/health` (aggregated status)
- GET `/actuator/health/liveness`
- GET `/actuator/health/readiness`
- GET `/actuator/info` (empty by default — populate via `info.*` properties or build info plugin)
- GET `/actuator/metrics` (list metric names)
- GET `/actuator/metrics/{metricName}` (detail for a metric, e.g. `jvm.memory.used`)

Health detail exposure: `management.endpoint.health.show-details=always` (intended for local/dev).
For production harden by:
```
management.endpoint.health.show-details=when_authorized
management.endpoints.web.exposure.include=health,info,metrics
```
And add security (Spring Security) before exposing publicly.

Add build info (optional) by adding to `pom.xml`:
```
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <layers enabled="true" />
  </configuration>
</plugin>
```
Then enable:
```
./mvnw spring-boot:build-info
```
This will enrich `/actuator/info`.

## Database & Migrations
- Managed by Liquibase: `db/changelog/db.changelog-master.xml`
- Automatically runs on startup (DDL validated: `spring.jpa.hibernate.ddl-auto=validate`)
- To generate a new incremental changeset, add a new `db.changelog-<version>-<name>.xml` and include it in master file.

## Caching
Spring Cache is enabled (see config). Exchange rate lookups are cached to reduce external
API calls (default simple in-memory cache). You can adjust cache provider via Spring Boot
configuration if desired.

## External Integration (HNB Exchange Rate API)
The service calls `https://api.hnb.hr/tecajn-eur/v3` to enrich products with a EUR to USD rate.
If the API is unreachable, a 503 ProblemDetail is returned for dependent endpoints
(e.g., product creation if conversion is required).
Property: `hnb.api.tecaj.v3.url`.

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## REST Endpoints
Base path: `/product`

1. Create Product
```
POST /product
Content-Type: application/json
{
  "name": "Gaming Keyboard",
  "code": "KEYB123456",  // must be exactly 10 chars
  "priceEur": 129.99,  // positive decimal with 2 decimal places
  "isAvailable": true
}
```
Curl example:
```shell
curl -i -X POST http://localhost:8080/product \
  -H 'Content-Type: application/json' \
  -d '{
        "name":"Gaming Keyboard",
        "code":"KEYB123456",
        "priceEur":129.99,
        "isAvailable":true
      }'
```
Response 201 Created (Location header with new resource URL).

2. Get Product by ID
```
GET /product/{id}
```

3. List Products (paginated)
```
GET /product?page=0&size=20&sort=id,desc
```
Response body is a Spring `Page` structure with metadata.

## Product Data Model (simplified)
```
Product {
  Long id;
  String code;        // unique 10-char code
  String name;
  BigDecimal priceEur;
  BigDecimal priceUsd; // derived via exchange rate
  Boolean isAvailable;
  LocalDateTime createdAt; // set on creation
  LocalDateTime updatedAt; // set on update
}
```

## Error Handling
Uses `ProblemDetail` (RFC 7807) responses. Common HTTP codes:
- 400 Validation or malformed request
- 404 Not found (product)
- 503 External dependency (exchange rate) unavailable
- 500 Unhandled server error

## Testing
Run all tests:
```
./mvnw test
```
Characteristics:
- Uses H2 in-memory database (see `application-test.properties`)
- Liquibase migrations also run in test scope ensuring parity

## Logging
Configured via `logback.xml`. Adjust levels at runtime with `--logging.level.org.tech.product_service=DEBUG`.

## Common Maven Commands
```
./mvnw clean                # Clean target
./mvnw test                 # Run tests
./mvnw spring-boot:run      # Dev run
./mvnw package              # Build jar
```

## Deployment (Outline)
1. Build jar: `./mvnw clean package`
2. (Optional) Create a Dockerfile to containerize the application (not yet included)
3. Provide externalized configuration via env vars / secrets