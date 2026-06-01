# Aesthetica — Agent Guide

Java 17 Maven WAR with embedded Tomcat 10 + Jersey 3 REST API + Hibernate 6 ORM + MySQL.

## Build & Run

```bash
mvn clean compile                                          # build
mvn exec:java -Dexec.mainClass="com.aesthetica.Main"      # run (port 8080)
mvn package -DskipTests                                    # produce target/aesthetica.war
```

App at `http://localhost:8080/aesthetica/`.

No test framework, linter, formatter, or CI.

## Database

- MySQL 5.7+ (`hibernate.cfg.xml`). Falls back to H2 in-memory if MySQL unreachable (`HibernateUtil`).
- Auto-schema via `hibernate.hbm2ddl.auto=update`.
- Seed: `scripts/populate_db.sh` runs migration + seed SQL from `src/main/resources/`.
- Plaintext passwords (not hashed). Credentials in `app.properties` (mailtrap SMTP, PayHere keys).

## Architecture

- **Entrypoint:** `Main.java` starts embedded Tomcat at `/aesthetica`, registers Jersey at `/api/*`, adds `ContextPathListener` + `AuthAccessFilter` as `ApplicationListener`s.
- **REST:** Jersey scans `com.aesthetica.controller` + `com.aesthetica.middleware` (`AppConfig`). Includes `TestController` at `/api/test`.
- **Auth:** `@IsUser` (name-bound) → `AuthFilter` checks session; 307 redirect to `sign_in.html`.
- **ORM:** `HibernateUtil` lazy singleton; services open/close sessions manually (no `@Transactional`).
- **JSON:** `AppUtil.GSON` singleton (built-in `LocalDate`/`LocalDateTime` adapters), used via `Response.ok().entity(json).build()`.

## Project Layout

```
src/main/java/com/aesthetica/
  Main.java                     – embedded Tomcat entrypoint
  Annotation/                   – @IsUser (case-sensitive: capital A)
  config/                       – AppConfig (Jersey ResourceConfig)
  controller/api/               – 6 Jersey controllers
  dto/                          – data-transfer objects (Gson-serialized)
  entity/                       – 15 JPA entities
  listener/                     – ContextPathListener
  mail/                         – email templating
  middleware/                   – AuthFilter, AuthAccessFilter, AccessControlFilter
  provider/                     – MailServiceProvider (Jakarta Mail via Mailtrap SMTP)
  service/                      – 8 business-logic classes
  util/                         – HibernateUtil, Env, PayHereUtil, AppUtil
  validation/                   – Validator (password 8+ upper+lower+digit+special, email, SL mobile)
src/main/resources/
  hibernate.cfg.xml, app.properties, db_migration_*.sql, db_seed_data.sql
src/main/webapp/
  9 static HTML + WEB-INF/ + assets/ (Bootstrap 5, local)
```

## Port Conflicts

```bash
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```
