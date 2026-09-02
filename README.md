# book-service-gateway

Basic API Gateway built with Java 21, Spring Boot 4.1.1 and Gradle.

## Routes

- `/api/user/**` -> `http://localhost:8081`
- `/api/inventory/**` -> `http://localhost:8082`

## Run

```bash
./gradlew :app:bootRun
```
