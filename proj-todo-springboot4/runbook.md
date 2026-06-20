# Kanban Task Board API — Runbook

## Overview

A RESTful Kanban board API built with Spring Boot 4.1.0. Manages columns and cards with position-based ordering.

- **Port:** 8002
- **Database:** H2 in-memory (resets on restart)
- **Java:** 21+
- **Build tool:** Maven

---

## Prerequisites

| Tool    | Version |
|---------|---------|
| Java    | 21+     |
| Maven   | 3.9+    |

---

## Start the Application

### Option 1 — Maven (development)

```bash
./mvnw spring-boot:run
```

### Option 2 — Packaged JAR

```bash
# Build
./mvnw clean package -DskipTests

# Run
java -jar target/kanban-api-1.0.0-SNAPSHOT.jar
```

### Option 3 — Full build + run

```bash
./mvnw clean package && java -jar target/kanban-api-1.0.0-SNAPSHOT.jar
```

The application starts at **http://localhost:8002**.

---

## Useful URLs

| Resource         | URL                                      |
|------------------|------------------------------------------|
| API base         | http://localhost:8002                    |
| Swagger UI       | http://localhost:8002/swagger-ui.html    |
| OpenAPI JSON     | http://localhost:8002/api-docs           |
| H2 Console       | http://localhost:8002/h2-console         |

**H2 Console connection settings:**
- JDBC URL: `jdbc:h2:mem:kanban`
- Username: `sa`
- Password: *(blank)*

---

## Run Tests

```bash
./mvnw test
```

### Run tests with coverage report

```bash
./mvnw verify
```

Coverage report is generated at `target/site/jacoco/index.html`.

The build enforces **≥ 90% instruction coverage** (JaCoCo check). The build fails if coverage drops below this threshold.

Test results (XML) are written to `target/surefire-reports/`.

---

## API Reference

### Columns

| Method | Path                          | Description                     |
|--------|-------------------------------|---------------------------------|
| GET    | /columns                      | List all columns                |
| POST   | /columns                      | Create a column                 |
| GET    | /columns/{id}                 | Get a column                    |
| PUT    | /columns/{id}                 | Replace a column                |
| PATCH  | /columns/{id}                 | Partial update a column         |
| DELETE | /columns/{id}                 | Delete a column (409 if cards exist) |
| PATCH  | /columns/reorder              | Reorder all columns             |
| GET    | /columns/{columnId}/cards     | List cards in a column          |
| PATCH  | /columns/{columnId}/cards/reorder | Reorder cards within a column |

### Cards

| Method | Path               | Description              |
|--------|--------------------|--------------------------|
| GET    | /cards             | List all cards (optionally filter by `column_id`) |
| POST   | /cards             | Create a card            |
| GET    | /cards/{id}        | Get a card               |
| PUT    | /cards/{id}        | Replace a card           |
| PATCH  | /cards/{id}        | Partial update a card    |
| DELETE | /cards/{id}        | Delete a card            |
| PATCH  | /cards/{id}/move   | Move card to a column/position |

### Notable behaviors

- **DELETE /columns/{id}**: Returns `409 Conflict` if the column contains any cards.
- **PATCH /columns/reorder**: The `column_ids` list must contain exactly all existing column IDs. Returns `422` otherwise.
- **PATCH /columns/{columnId}/cards/reorder**: The `card_ids` list must contain exactly all card IDs in that column. Returns `422` otherwise.
- **PATCH /cards/{id}/move**: Handles same-column reordering and cross-column moves, keeping all positions gap-free.
- Card priority values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` (optional field).
- All JSON fields use `snake_case`.
- Positions are 0-indexed.

---

## Project Structure

```
src/
  main/java/com/kanban/
    KanbanApplication.java          # Entry point
    controller/
      ColumnController.java
      CardController.java
    service/
      ColumnService.java
      CardService.java
    entity/
      BoardColumn.java
      Card.java
    repository/
      ColumnRepository.java
      CardRepository.java
    dto/
      request/                       # ColumnCreateRequest, CardCreateRequest, ...
      response/                      # ColumnResponse, CardResponse
    exception/
      GlobalExceptionHandler.java
      ResourceNotFoundException.java
      ConflictException.java
  main/resources/
    application.properties
  test/java/com/kanban/
    service/
      ColumnServiceTest.java         # 21 tests
      CardServiceTest.java           # 21 tests
    controller/
      ColumnControllerTest.java      # 15 tests
      CardControllerTest.java        # 16 tests
```
