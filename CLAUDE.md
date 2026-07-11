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

Requires the `ANTHROPIC_API_KEY` environment variable to be set (used by both AI integration paths below).

## Architecture

This app has two independent feature areas:

### 1. User CRUD (`/api/users`)

Standard Spring Boot layered architecture with four layers:

- **Controller** (`controllers/UserController.java`) — REST endpoints, returns `ResponseEntity`
- **Service** (`services/UserService.java`) — business logic, orchestrates repository calls
- **Repository** (`repositories/UserRepository.java`) — extends `JpaRepository<User, Long>`, no custom queries currently
- **Model** (`models/User.java`) — JPA entity with Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)

Dependencies flow strictly downward: Controller → Service → Repository → Model.

### 2. AI chat — two parallel implementations of the same feature

This repo intentionally implements the same chat/system-prompt/reset functionality twice, using two different integration styles, for learning/comparison purposes:

- **`/api/ai`** (`AiController` → `AiService`) — uses **Spring AI**'s `ChatClient`, autoconfigured from `spring.ai.anthropic.*` properties in `application.properties`. No explicit client bean; Spring Boot wires it automatically from the `spring-ai-starter-model-anthropic` dependency.
- **`/api/claude`** (`ClaudeController` → `ClaudeService`) — uses the **raw `anthropic-java` SDK** directly. The `AnthropicClient` bean and model name are manually configured in `config/AnthropicClientConfig.java`, reading `claude.configuration.api-key` / `claude.configuration.model` properties.

Both services hold conversation history and system prompt as **in-memory instance state** (not persisted, not thread-safe, single global conversation per service instance) — this is a learning app, not production chat infra. Each exposes the same three operations: `chat`, `setSystemPrompt`, `reset`.

When adding a new AI-related feature, decide up front whether it belongs in the Spring AI path or the raw SDK path, and mirror the existing pattern (controller → service, constructor injection, same three endpoints) rather than introducing a third style.

## Tech Stack

- Java 17, Spring Boot 3.5.4, Maven
- Spring Data JPA + H2 in-memory database (schema auto-created via `spring.jpa.hibernate.ddl-auto=update`)
- Lombok for boilerplate reduction — entity fields use `@Data`, DTOs/new classes should follow the same pattern
- Spring AI (`spring-ai-starter-model-anthropic`, BOM version pinned via `spring-ai.version` in `pom.xml`) and the standalone `anthropic-java` SDK, used side by side (see AI chat section above)
