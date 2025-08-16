# Participation Service

The **Participation Service** manages user participation in quizzes, tracking attempts, scores, and progress. It provides APIs for recording and retrieving participation data, and integrates with other services for analytics and rewards.

## Features

- Record user quiz attempts and scores
- Track participation history
- RESTful API endpoints
- Integration with Quiz, LeaderBoard, and User services
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

| Method | Endpoint              | Description                   |
| ------ | --------------------- | ----------------------------- |
| GET    | /participation        | Get all participation records |
| POST   | /participation        | Record a new participation    |
| GET    | /participation/{user} | Get participation by user     |

## Project Structure

```
participation-service/
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
