# banking-oauth2-oidc-microservices
Enterprise-grade banking microservices architecture using Spring Boot, OAuth2, OIDC (Keycloak), API Gateway, 
client-credentials flow, scope &amp; ownership-based authorization, observability, Docker, Kubernetes, and CI/CD.


# Banking OAuth2 OIDC Microservices

Enterprise-grade banking microservices architecture using Spring Boot, OAuth2, OIDC (Keycloak), API Gateway, client-credentials flow, scope & ownership-based authorization, observability, Docker, Kubernetes, and CI/CD.

---

## Project Goals

This project demonstrates:

- OAuth2 + OIDC authentication using Keycloak
- JWT validation at API Gateway and microservices (Zero Trust)
- Role-based, Scope-based, and Ownership-based authorization
- Client Credentials flow for secure service-to-service communication
- Independent Spring Boot services in a monorepo structure
- H2 database with seeded test data
- Caffeine caching
- OpenAPI (Swagger) documentation
- Prometheus + Grafana observability
- Docker & Kubernetes deployment setup
- GitHub Actions CI pipeline

---

## High-Level Architecture

User → Keycloak → API Gateway → Microservices

---

## Architecture Overview

                 +------------------+
                 |     Keycloak     |
                 |  (OIDC Provider) |
                 +------------------+
                           |
                           v
    Client ---> API Gateway ---> Account Service
                           |
                           v
                Transaction Service

---

### Security Layers

1. JWT validation at API Gateway
2. JWT validation at each microservice (Zero Trust)
3. Scope-based authorization
4. Role-based authorization
5. Ownership-based authorization (business-level)
6. Client Credentials flow for service-to-service communication

---

## Services

- API Gateway
- Account Service
- Transaction Service
- Common Library

Detailed documentation available under `/docs`.

---

## Documentation

- Architecture → docs/architecture.md
- Security → docs/security.md
- Service Responsibilities → docs/service-responsibilities.md
- Architecture Decisions → docs/decision-log.md