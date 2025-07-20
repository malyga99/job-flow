# JobFlow

[//]: # (> Microservice-based platform to track job applications, automate your job search, and stay organized — with analytics, notifications, and OAuth login.)


## Description

**JobFlow** is a backend platform that helps users **track job applications, manage their career progress, and gain
insights through structured analytics** in one place.

### Who is it for?

This platform is designed for:

- **Junior developers** – track job hunt progress and stay organized.
- **Career changers** – monitor progress and refine job search strategy.
- **Job seekers** – receive timely notifications and manage application flow.

### How it works

1. Users authenticate via email or third-party accounts (Google, GitHub).
2. Applications are created, updated, or removed through the job-tracker-service.
3. Status history and timestamps are used to generate analytics.
4. Notifications are delivered via email and Telegram.
5. Users can monitor and manage their job search progress.

---

## Technology Stack

### Backend

- **Java 21** — main programming language
- **Spring Boot** — core framework
- **Maven** — dependency and build management

### Authentication & Security

- **JWT (access / refresh)** — stateless token-based authentication
- **OAuth (OpenID) - Google/GitHub** — authentication via Google/GitHub accounts
- **Redis Blacklist** — refresh-token invalidation on logout
- **Spring Security** — RBAC, filters, exception handling
- **Redis Rate Limiter (Fixed Window)** — protection against brute‑force and abuse

### Messaging & Event‑Driven Communication

- **RabbitMQ** — message broker for cross‑service events with DLQ support
- **Spring AMQP + Spring Retry** — message publishing / consuming & automatic retries
- **Telegram Bot API** — direct Telegram notifications
- **JavaMailSender (SMTP)** — email notifications and verification codes with Google SMTP

### Database & Storage

- **PostgreSQL** — primary relational database
- **Redis** — caching for tokens, verification codes, analytics and rate limits

### DevOps & Deployment

- **Docker + Docker Compose** — containerisation & orchestration
- **CI/CD with GitHub Actions** — CI (feature branches / PR → develop), CD (PR -> main)
- **Git Flow + Conventional Commits** — consistent branching and commit conventions

### Testing

- **JUnit 5 + Mockito** — unit testing
- **Testcontainers** — integration testing with real services
- **Awaitility** — async assertions for RabbitMQ tests
- **JaCoCo + Codecov** — 100% test coverage & reporting

### Documentation & Logging

- **Swagger / OpenAPI** — API documentation
- **Logback** — structured logging
- **ELK Stack (Elasticsearch + Logstash + Kibana + Filebeat)** — centralized log aggregation

---

## Microservices

| Service                  | README                                    | Description                                  |
|--------------------------|-------------------------------------------|----------------------------------------------|
| **user-service**         | [click](./user-service/README.md)         | User auth, registration, token management    |
| **job-tracker-service**  | [click](./job-tracker-service/README.md)  | Job applications CRUD & analytics            |
| **notification-service** | [click](./notification-service/README.md) | Email & Telegram notifications, DLQ          |
| **resume-service**       | *WIP*                                     | Resume generation (PDF, markdown) (planned)  |
| **gateway-service**      | *WIP*                                     | API Gateway, routing, and fallback (planned) |
