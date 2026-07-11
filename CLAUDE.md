# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvn clean package

# Run (dev server on http://localhost:8080)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=DemoApplicationTests

# Run a single test method
mvn test -Dtest=DemoApplicationTests#methodName
```

H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, no password).

## Architecture

Standard Spring Boot layered architecture with four layers:

- **Controller** (`controllers/`) — REST endpoints at `/api/users`, return `ResponseEntity`
- **Service** (`services/`) — business logic, orchestrates repository calls
- **Repository** (`repositories/`) — extends `JpaRepository<User, Long>`, no custom queries currently
- **Model** (`models/`) — JPA entities with Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)

Dependencies flow strictly downward: Controller → Service → Repository → Model. Use constructor injection throughout (existing code uses `@Autowired` on constructors).

## Tech Stack

- Java 17, Spring Boot 3.5.4, Maven
- Spring Data JPA + H2 in-memory database (schema auto-created via `spring.jpa.hibernate.ddl-auto=update`)
- Lombok for boilerplate reduction — entity fields use `@Data`, DTOs/new classes should follow the same pattern
