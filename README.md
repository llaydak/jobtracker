# Job Tracker — Microservice Backend & CI/CD Architecture

A containerized job application and interview tracking system built with Java 17 and Spring Boot, featuring Redis caching, a decoupled notification microservice, and production-grade CI/CD and Kubernetes configurations.

## Architecture & Tech Stack
- **Core Service (`8080`):** Java 17, Spring Boot 3.4.5, Spring Data JPA (N+1 query optimization via `JOIN FETCH`), Rich Domain Model (DDD), and loose coupling via interface abstraction.
- **Persistence & Caching:** PostgreSQL 16 (primary relational store) & Redis 7 (Cache-Aside pattern with TTL and container memory limits).
- **Notification Service (`8081`):** Independent microservice triggered on application status updates via synchronous HTTP (`RestClient`) with graceful degradation.
- **DevOps & Orchestration:** Multi-stage `Dockerfile`s, Docker Compose (`healthcheck`, `mem_limit`, horizontal scaling), Declarative `Jenkinsfile` (JaCoCo, SonarQube Quality Gate, Nexus artifact publishing), and Kubernetes `Deployment`/`Service` manifests.

## API Endpoints (Core Service)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/companies` | Create a new company |
| `GET` | `/api/companies` | List all companies |
| `POST` | `/api/applications` | Submit a new job application |
| `GET` | `/api/applications` | Retrieve all applications (Redis cached) |
| `PATCH` | `/api/applications/{id}/status?status={STATUS}` | Update status & trigger notification service |
| `DELETE` | `/api/applications/{id}` | Delete an application |
| `POST` | `/api/applications/{applicationId}/interviews` | Schedule an interview stage |
| `GET` | `/api/applications/{applicationId}/interviews` | List interviews for an application |

## Quick Start
```bash
docker compose up --build -d