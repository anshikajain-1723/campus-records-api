# Verification — 18 October 2026

- Java 21 / Spring Boot 4.1.1.
- `./mvnw verify`: BUILD SUCCESS, 8 tests, 0 failures/errors/skips.
- Flyway migration + Hibernate schema validation ran against H2.
- Packaged executable JAR started successfully on localhost.
- Actual HTTP smoke checks:
  - POST student → 201
  - GET filtered page → 200 / 1 match
  - PUT student → 200 / updated status
  - POST duplicate → 409
  - DELETE student → 204
  - GET deleted student → 404

MySQL profile is included but not run against a MySQL server. No public API deployment or frontend integration is claimed.
