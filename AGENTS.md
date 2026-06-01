# Aesthetica — Agent Guide

Java 17 Maven WAR with embedded Tomcat 10 + Jersey 3 REST API + Hibernate 6 ORM + MySQL.

## Build & Run

```bash
mvn clean compile                                          # build
mvn exec:java -Dexec.mainClass="com.aesthetica.Main"      # run (port 8080)
mvn package -DskipTests                                    # produce target/aesthetica.war
```

App is served at `http://localhost:8080/aesthetica/` (context path is `/aesthetica`).

No test framework (no JUnit). No linter, formatter, or CI.

## Database

- MySQL 5.7+ (`hibernate.cfg.xml`). Falls back to H2 in-memory if MySQL unreachable (`HibernateUtil`).
- Auto-schema via `hibernate.hbm2ddl.auto=update` (Hibernate creates/alters tables from entity annotations).
- Seed: `scripts/populate_db.sh` runs `db_migration_*.sql` + `db_seed_data.sql`.
- Plaintext passwords (not hashed). Credentials in `app.properties` (mailtrap, PayHere keys).

## Architecture

- **Entrypoint:** `Main.java` starts embedded Tomcat, registers Jersey at `/api/*`, applies servlet filters.
- **REST:** Jersey scans `com.aesthetica.controller` + `com.aesthetica.middleware` (configured in `AppConfig`).
- **Auth:** Session-based (`request.getSession()`). `@IsUser` annotation → `AuthFilter` checks session; rejects with 307 to `sign_in.html`.
- **ORM:** Hibernate `SessionFactory` via `HibernateUtil` (lazy singleton, open/close sessions manually).
- **DTOs** decouple API from entities (Gson serialization).

## Project Layout

```
src/main/java/com/aesthetica/
  Main.java                     – entrypoint (embedded Tomcat)
  entity/                       – 15 JPA entities
  service/                      – 8 business-logic classes
  controller/api/               – 6 Jersey controllers (~25 endpoints)
  middleware/                    – auth filters
  util/                         – HibernateUtil, Env, PayHereUtil, AppUtil
  dto/                          – data-transfer objects
src/main/resources/
  hibernate.cfg.xml             – DB connection, entity mappings
  app.properties                – mailtrap + PayHere credentials
  db_migration_*.sql            – schema migration
  db_seed_data.sql              – initial data
src/main/webapp/
  8 static HTML pages, assets/  – CSS, JS, images (Bootstrap 5 locally)
```

## Key Conventions

- Services **open and close Hibernate sessions manually** (no `@Transactional`).
- JSON responses built with `Gson` `JsonObject`; returned as `Response.ok().entity(json).build()`.
- All controllers bind `@Context HttpServletRequest` when they need the session.
- `HibernateUtil` uses a lazy singleton `SessionFactory` with H2 fallback (no service start failure if DB is down).
- Validation regexes in `validation/Validator.java`: password (8+, upper+lower+digit+special), email, Sri Lankan mobile.

## Reference Docs

- `DOCUMENTATION.md` — architecture overview
- `API_REFERENCE.md` — all endpoints with request/response
- `DATABASE_SCHEMA.md` — full schema reference
- `SETUP_GUIDE.md` — prerequisites and install steps
- `QUICK_REFERENCE.md` — dev tasks and cURL examples

## Port Conflicts

```bash
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```
