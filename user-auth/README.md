# User Auth Service

The **User Auth Service** handles authentication and authorization for the Quizzler platform. It manages user registration, login, token issuance, and user roles, and integrates with other services for secure access control.

## Features

- User registration and login
- JWT token generation and validation
- Role-based access control
- RESTful API endpoints
- Integration with API Gateway and other services
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

| Method | Endpoint       | Description           |
| ------ | -------------- | --------------------- |
| POST   | /auth/register | Register a new user   |
| POST   | /auth/login    | User login            |
| GET    | /auth/me       | Get current user info |

## Project Structure

```
user-auth/
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
