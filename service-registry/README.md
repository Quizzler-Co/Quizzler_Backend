# Service Registry

The **Service Registry** is responsible for service discovery in the Quizzler microservices architecture. It allows services to register themselves and discover other services dynamically, enabling load balancing and failover.

## Features

- Service registration and discovery
- Health checks
- RESTful endpoints for service management
- Built with Java and Spring Boot (Eureka/Consul compatible)

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

## Project Structure

```
service-registry/
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
