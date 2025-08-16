# API Gateway Service

The **API Gateway** acts as the single entry point for all client requests in the Quizzler backend system. It routes requests to the appropriate microservices, handles authentication, and provides cross-cutting concerns such as logging and rate limiting.

## Features

- Centralized routing for all backend services
- Authentication and authorization
- Load balancing and failover
- Request/response logging
- CORS and security headers

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build & Run

```sh
./mvnw clean install
./mvnw spring-boot:run
```

## Configuration

- Main configs in `src/main/resources/application.yml` or `application.properties`
- Routing rules and service URLs are customizable

## Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
```

## Contributing

See the main repository guidelines.

## License

MIT License
