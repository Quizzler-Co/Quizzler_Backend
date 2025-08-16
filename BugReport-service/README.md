# BugReport Service

The **BugReport Service** manages bug reports submitted by users or detected by the system. It allows for creating, updating, viewing, and resolving bug reports, and integrates with other services for notifications and tracking.

## Features

- Submit, update, and resolve bug reports
- RESTful API endpoints
- Integration with notification and user services
- Built with Java and Spring Boot

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build & Run

```sh
./mvnw clean install
./mvnw spring-boot:run
```

## API Endpoints

| Method | Endpoint   | Description          |
| ------ | ---------- | -------------------- |
| GET    | /bugs      | List all bug reports |
| POST   | /bugs      | Submit a new bug     |
| PUT    | /bugs/{id} | Update a bug report  |
| DELETE | /bugs/{id} | Delete a bug report  |

## Project Structure

```
BugReport-service/
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
