# Hotel Microservices

This repository contains a set of Spring Boot services that together form a simple microservice ecosystem.

## Services

### Config Service
Provides centralized configuration using **Spring Cloud Config Server**. The configuration files are loaded from the `configurations` directory or from a Git repository.
- **Port:** `8888`

### Discovery Service
Eureka server that allows other services to register and discover each other.
- **Port:** `8761`
- Uses the Config Service to load its configuration.

### Auth Service
Authentication service with JWT based security.
- Exposes endpoints for user registration and login.
- Configured with Spring Security and generates/validates JWT tokens.

## Building
Each service is a standalone Maven project.
Use the Maven wrapper in each module to build:

```bash
cd service/<module>
./mvnw package
```

Replace `<module>` with `config-service`, `discovery`, or `auth`.
The resulting JAR file will be placed in `target/`.

## Running with Docker Compose
A `docker-compose.yml` is provided to run the Config Service and the Discovery Service together:

```bash
docker-compose up --build
```

This will expose:
- Config Service on [http://localhost:8888](http://localhost:8888)
- Discovery Service on [http://localhost:8761](http://localhost:8761)

You can adapt the compose file to include the Auth Service or run the services manually using the JAR files produced by Maven.

