# Docker Setup Guide for Quizzler Backend

This guide explains how to run all Quizzler backend services using Docker Compose.

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- At least 8GB RAM available for Docker
- Ports 8081-8089, 8761, 27017, 9092, 2181 should be available

## Services Included

1. **service-registry** (Eureka) - Port 8761
2. **api-gateway** - Port 8086
3. **user-auth** - Port 8098
4. **quiz-service** - Port 8081
5. **question-service** - Port 8082
6. **participation-service** - Port 8083
7. **leaderboard** - Port 8084
8. **java-judge** - Port 8085
9. **bugreport-service** - Port 8088
10. **mongodb** - Port 27017
11. **kafka** - Port 9092
12. **zookeeper** - Port 2181

## Quick Start

### 1. Build and Start All Services

```bash
docker-compose up --build
```

This will:
- Build Docker images for all services
- Start service-registry first (with health checks)
- Start dependencies (MongoDB, Kafka, Zookeeper)
- Start all microservices in dependency order

### 2. Start in Detached Mode (Background)

```bash
docker-compose up -d --build
```

### 3. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f user-auth
```

### 4. Stop All Services

```bash
docker-compose down
```

### 5. Stop and Remove Volumes

```bash
docker-compose down -v
```

## Service Dependencies

- **service-registry** must be healthy before other services start
- **api-gateway** depends on service-registry and user-auth
- **participation-service** depends on service-registry, kafka, and mongodb
- **leaderboard** depends on service-registry and kafka
- **bugreport-service** depends on service-registry and mongodb

## Health Checks

### Eureka Dashboard
- URL: http://localhost:8761
- Check if all services are registered

### API Gateway Health
- URL: http://localhost:8086/api/gateway/health

### Individual Service Health
Each service exposes actuator endpoints (if configured):
- http://localhost:8081/actuator/health (quiz-service)
- http://localhost:8082/actuator/health (question-service)
- etc.

## Troubleshooting

### Services Not Starting

1. **Check service-registry is healthy:**
   ```bash
   docker-compose ps service-registry
   curl http://localhost:8761
   ```

2. **Check logs for errors:**
   ```bash
   docker-compose logs service-registry
   docker-compose logs api-gateway
   ```

3. **Verify network connectivity:**
   ```bash
   docker network inspect quizzler_quizzler-network
   ```

### Services Not Registering with Eureka

1. Ensure service-registry is running and healthy
2. Check Eureka dashboard: http://localhost:8761
3. Verify service logs for connection errors:
   ```bash
   docker-compose logs user-auth | grep -i eureka
   ```

### Port Conflicts

If ports are already in use:
1. Stop conflicting services
2. Or modify port mappings in `docker-compose.yml`

### Out of Memory

If services fail to start:
1. Increase Docker Desktop memory allocation
2. Or reduce number of services running simultaneously

## Rebuilding Specific Service

```bash
# Rebuild and restart specific service
docker-compose up -d --build --no-deps service-name

# Example
docker-compose up -d --build --no-deps api-gateway
```

## Database Configuration

- **MongoDB**: Running in Docker, accessible at `mongodb:27017` from services
- **MySQL**: External (Aiven Cloud) - configured in user-auth service
- **H2**: In-memory database for java-judge service

## Kafka Configuration

- Kafka is accessible at `kafka:9092` from services
- Topics are auto-created by services
- Zookeeper manages Kafka coordination

## Network Configuration

All services are on the `quizzler-network` bridge network and can communicate using service names:
- `service-registry:8761`
- `api-gateway:8086`
- `user-auth:8098`
- `mongodb:27017`
- `kafka:9092`
- etc.

## Environment Variables

Services can be configured using environment variables in `docker-compose.yml`. Currently, most configuration is in `application.properties`/`application.yml` files.

## Production Considerations

For production deployment:
1. Use environment variables for sensitive data (passwords, API keys)
2. Configure proper health checks and restart policies
3. Use Docker secrets for credentials
4. Set up proper logging aggregation
5. Configure resource limits (CPU, memory)
6. Use external databases instead of containerized ones
7. Set up monitoring and alerting




