# Campus Records API

An original **Java 21 / Spring Boot 4.1** student-records CRUD service created for **Anshika Jain’s personal portfolio**, with AI assistance. It demonstrates backend fundamentals alongside an Angular-focused professional profile. It is not employer code, paid client work, or a claim of professional backend specialization.

## Features

- REST create, read, update and delete operations with meaningful HTTP status codes
- Request/response DTOs separated from JPA entities
- Bean Validation with field-level error details
- Case-normalized unique emails enforced in both service and database
- Combined text search and status filter; bounded, stable pagination
- Transactional writes and centralized Problem Detail error responses
- Flyway-managed schema; Hibernate validates it instead of silently changing it
- Zero-setup H2 demo, plus an environment-driven MySQL profile
- Maven Wrapper, integration tests and GitHub Actions verification

## Quick start

Requires Java 21. Maven is downloaded by the included wrapper on the first run.

```sh
./mvnw verify
./mvnw spring-boot:run
```

The API listens on **http://127.0.0.1:8080**. H2 starts empty and resets on restart. It is intentionally bound to localhost because this demo has no authentication. Use fictional records only.

### Try the API

```sh
curl -i -X POST http://127.0.0.1:8080/api/v1/students \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Alex Morgan","email":"alex@example.com","course":"Computer Science","academicYear":2,"status":"ACTIVE","enrolledOn":"2025-08-10"}'

curl 'http://127.0.0.1:8080/api/v1/students?search=alex&status=ACTIVE&page=0&size=10'
```

Use the returned `id` for these calls; `1` is only an example.

```sh
curl http://127.0.0.1:8080/api/v1/students/1

curl -X PUT http://127.0.0.1:8080/api/v1/students/1 \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Alex Morgan","email":"alex@example.com","course":"Computer Science","academicYear":3,"status":"ON_LEAVE","enrolledOn":"2025-08-10"}'

curl -i -X DELETE http://127.0.0.1:8080/api/v1/students/1
```

Import [the Postman collection](docs/campus-records.postman_collection.json) for the same flow. Its create request saves the returned student ID for later requests.

## API contract

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/v1/students` | 201 and Location header |
| GET | `/api/v1/students` | 200, paginated `items` and totals |
| GET | `/api/v1/students/{id}` | 200 or 404 |
| PUT | `/api/v1/students/{id}` | Full update, 200 or 404 |
| DELETE | `/api/v1/students/{id}` | 204 or 404 |

List supports `search` (name/email, literal substring, max 100 characters), optional `status`, zero-based `page`, and `size` (1–100). Stable sorting is by ID ascending. Status values: `ACTIVE`, `ON_LEAVE`, `GRADUATED`.

Required request fields: `fullName` and `course` (2–80 characters after trimming), `email` (valid, max 254 characters), `academicYear` (1–4), `status`, and `enrolledOn` (ISO date, today or earlier). Email is normalized to lowercase. PUT requires every field; PATCH is not implemented.

Errors use Problem Detail JSON. Invalid body/parameters return 400; missing records return 404; duplicate emails and database conflicts return 409. Validation responses include an `errors` object keyed by field name. Database internals are not included in conflict responses.

## Architecture

```text
HTTP → StudentController → StudentService → StudentRepository → database
         DTO validation     transactions    Spring Data JPA      Flyway schema
                ↘ ApiErrorHandler → Problem Detail JSON
```

The service checks uniqueness for helpful responses, and the database constraint protects against simultaneous duplicate inserts. Update timestamps are maintained through JPA lifecycle callbacks. This demo uses last-write-wins updates; optimistic locking is a future improvement.

## MySQL profile

Create a dedicated empty database and restricted database user yourself. Set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` in your local environment (never commit credentials), then run:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Example URL format: `jdbc:mysql://localhost:3306/campus_records`. Flyway applies the schema to the selected database. H2 is the verified local/test database; the MySQL profile has not been integration-tested against a running MySQL instance in this workspace.

## Verification and limits

See [QA results](docs/QA.md). Integration tests use the real Spring context, HTTP mapping/validation, Flyway schema and H2 repository; the service and repository are not mocked.

This is a learning/portfolio service, with no login, authorization, audit log, rate limiting, production deployment or durable default storage. It is a separate project from the [Campus Desk Angular demo](https://github.com/anshikajain-1723/campus-desk); the Angular demo still uses local storage and is **not wired to this API**.

## Credits

Original application code created with OpenAI Codex assistance. The Maven Wrapper and application skeleton were generated by [Spring Initializr](https://start.spring.io/). Framework dependencies retain their respective licenses. No external project repository or proprietary material was copied.

References: [Spring Boot](https://spring.io/projects/spring-boot) · [Spring validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html).
