# Job Tracker

A **production-oriented learning project**: a job application and interview tracker built with Java 17 and Spring Boot, used to practice layered backend design, JPA relationship mapping, Redis caching, a containerized local environment, and a CI/CD pipeline *design*.

> Scope note: this is a learning project, not a production system. The "Known Limitations" section below describes exactly what is and isn't implemented, so the code and the README do not contradict each other.

## Tech Stack

| Area | Used in this project | Notes |
| :--- | :--- | :--- |
| Language / Framework | Java 17, Spring Boot 4.2.0 | Parent version is `4.2.0`; the notification service uses `3.4.5` |
| Web / API | Spring MVC, REST, Bean Validation | Controller / Service / Repository layering, DTO `record`s, `@Valid` request bodies |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL 16 | `LAZY` associations, `JOIN FETCH` to avoid N+1 |
| Caching | Redis 7 via Spring Cache (`@Cacheable` / `@CacheEvict`) | Used **only as a cache** (Cache-Aside), not as a primary datastore |
| Service-to-service | `RestClient` over HTTP | Synchronous call to the notification service; failures are caught and logged |
| Build / Container | Maven, multi-stage `Dockerfile`, Docker Compose | Local environment with healthchecks |
| CI/CD (designed) | `Jenkinsfile`, JaCoCo, SonarQube quality gate, Nexus | Written and configured, but **not** run against live Jenkins/Sonar/Nexus |

## Architecture

```
Client ──► app (:8080) ──► PostgreSQL (primary store)
                ├──► Redis (Cache-Aside cache for GET /api/applications)
                └──► notification-service (:8081) ──► logs the event
```

- **Layering:** `controller` (HTTP + validation) → `service` (business logic, transactions, caching) → `repository` (Spring Data JPA). Applications use `record` DTOs; entities are not exposed for them.
- **Domain logic:** `JobApplication.prepareForInterview()` puts a small piece of behaviour on the entity (an interview cannot be added to a rejected application). This is a deliberate attempt to apply DDD *principles* in a small codebase — not a full DDD design.
- **Decoupling:** notifications go through the `NotificationSender` interface with `HttpNotificationSender` as the implementation. This is where dependency inversion is applied.
- **N+1:** `JobApplicationRepository.findAllWithCompany()` uses `JOIN FETCH` so listing applications does not issue one query per row.

## API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/companies` | List companies |
| `POST` | `/api/companies` | Create a company |
| `GET` | `/api/applications` | List applications (served from Redis cache) |
| `POST` | `/api/applications` | Create an application (`companyId` + `position` required) |
| `PATCH` | `/api/applications/{id}/status?status=INTERVIEW` | Update status, evict cache, notify the notification service |
| `DELETE` | `/api/applications/{id}` | Delete an application |
| `GET` | `/api/applications/{applicationId}/interviews` | List interviews for an application |
| `POST` | `/api/applications/{applicationId}/interviews` | Add an interview stage |

`ApplicationStatus` values: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`.

### Example

```bash
# create a company
curl -X POST localhost:8080/api/companies \
  -H 'Content-Type: application/json' \
  -d '{"name":"Acme Corp","industry":"Tech"}'

# create an application for company 1
curl -X POST localhost:8080/api/applications \
  -H 'Content-Type: application/json' \
  -d '{"companyId":1,"position":"Backend Developer"}'

# move it to INTERVIEW (also triggers the notification service)
curl -X PATCH 'localhost:8080/api/applications/1/status?status=INTERVIEW'
```

## Running Locally

Requires Docker with Compose.

```bash
cp .env.example .env        # then edit the password
docker compose up --build -d
```

Services: `app` → `http://localhost:8080`, `notification-service` → internal port `8081`, PostgreSQL → `5432`, Redis → `6379`.

### Troubleshooting: `curl: (7) Failed to connect to localhost port 8080`

The app container exiting right after `docker compose up` prints `Started` is usually **not** an application bug. Check the app status and logs first:

```bash
docker compose ps -a            # is jobtracker-app-1 "Up" or "Exited (1)" / "Created"?
docker compose logs app | grep -i 'Caused by'
```

If the log ends with `Caused by: java.net.UnknownHostException: postgres`, the app cannot resolve the `postgres` service, meaning PostgreSQL is **not attached to the Compose network** (this can happen when a recreate happened while the named containers survived on a stale network). Verify and repair:

```bash
# a healthy db should list an IP here, e.g. 172.19.0.3
docker network inspect jobtracker_default --format '{{json .Containers}}'

docker compose down --remove-orphans
docker compose up -d
docker compose ps -a
docker logs -f jobtracker-app-1   # wait for "Started JobtrackerApplication in ... seconds"
```

`docker compose restart app` is **not** enough — it keeps the detached network. Use `down` then `up`.

If the app stays `Created` / `Exited (1)` and the log shows something else (missing env var, port already bound, OOM), that log line is the real cause.

### Configurable host ports

If something on your machine already uses `5432` / `6379` / `8080`, override the **host** ports via `.env` (container ports stay the same):

```bash
# .env
APP_PORT=18080
POSTGRES_PORT=55432
REDIS_PORT=56379
```

Every variable has a default (`${VAR:-default}`), so `docker compose up` works even without a `.env` file. See `.env.example`.

### Scaling the app

The `app` service deliberately has **no `container_name`**, which is what allows multiple replicas. A published host port can only be bound once, so scaling requires dropping the port mapping via a small override file:

```bash
# docker-compose.scale.yml
services:
  app:
    ports: !override []

# run 3 replicas behind the compose network
docker compose -f docker-compose.yml -f docker-compose.scale.yml up -d --scale app=3
```

Replicas share the same PostgreSQL and Redis, so cache eviction on one instance is visible to all. In Kubernetes, replicas sit behind the `ClusterIP` `Service` in `k8s/deployment.yaml` (2 replicas) instead.

## Configuration

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/job_tracker` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `postgres` / `postgres` | DB credentials |
| `SPRING_DATA_REDIS_HOST` / `_PORT` | `localhost` / `6379` | Redis connection |
| `CACHE_TTL_MS` | `600000` (10 min) | Cache entry TTL |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8081` | Notification service base URL |

## CI/CD Pipeline (designed, not yet executed)

`Jenkinsfile` describes the intended stages: checkout → build & unit tests with JaCoCo → SonarQube analysis → quality gate → `deploy` to Nexus (`distributionManagement` is configured in `pom.xml`) → Docker image build.

These stages have **not** been run against a live Jenkins controller with SonarQube and Nexus. Treat the file as a design artifact to discuss, not as evidence of hands-on operation.
## Known Limitations

Being explicit about these is intentional — it is easier to defend than to be caught out:

- **Tests are minimal.** Only a `contextLoads()` test exists. It is a full `@SpringBootTest`, so it needs a reachable PostgreSQL (and Redis) to pass. On a machine without them `mvn test` fails, which means the Jenkins test stage would fail as-is. Replacing it with lighter slice tests (`@WebMvcTest` / `@DataJpaTest`, H2 or containers) is the next planned step.
- **Notification service is a stub.** It logs the received event — no email/push/SMS integration. It demonstrates service separation and an HTTP boundary, not a mature microservices platform.
- **Kubernetes manifests are partial.** `k8s/deployment.yaml` covers only the app (`Deployment` + `Service`). There are no manifests for PostgreSQL, Redis, or the notification service, and no `ConfigMap`/`Secret`, so the DB credentials are not injected into the app deployment yet.
- **Redis is a cache only.** It is not used as a NoSQL datastore.
- **`ddl-auto=update`.** The schema is generated by Hibernate; there is no migration tool (Flyway/Liquibase).
- **One entity is exposed.** `CompanyController` returns the `Company` entity directly instead of a DTO.
- **Caching and transactions are applied per service method** rather than consistently at a single service boundary.

## Roadmap

1. Slice tests that do not need a live DB, so the CI test stage can pass.
2. Flyway migrations + `ddl-auto=validate`.
3. `CompanyResponse` DTO so no entity is exposed from the API.
4. Kubernetes manifests for Postgres / Redis / notification, plus a `Secret` for credentials.

## Requirements

- JDK 17
- Docker + Docker Compose (for the containerized workflow)
- The Maven wrapper (`./mvnw`) is included, so no local Maven install is needed for the build.
