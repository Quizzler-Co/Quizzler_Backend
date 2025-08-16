# Question Service

The **Question Service** is a core microservice in the Quizzler backend system, responsible for managing quiz questions. It provides RESTful APIs for creating, retrieving, updating, and deleting questions, and supports integration with other services such as Quiz, LeaderBoard, and Participation.

## Features

- Add, update, delete, and retrieve quiz questions
- Support for multiple question types (e.g., multiple choice, true/false, short answer)
- RESTful API endpoints
- Integration with other Quizzler microservices
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

| Method | Endpoint        | Description           |
| ------ | --------------- | --------------------- |
| GET    | /questions      | Get all questions     |
| GET    | /questions/{id} | Get question by ID    |
| POST   | /questions      | Create a new question |
| PUT    | /questions/{id} | Update a question     |
| DELETE | /questions/{id} | Delete a question     |

> **Note:** See the source code or API documentation for detailed request/response formats.

## Project Structure

```
question-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
```

## Configuration

- Application properties can be set in `src/main/resources/application.properties` or `application.yml`.
- Service ports and database configs are customizable.

## Contributing

See the main repository guidelines.

## License

MIT License
