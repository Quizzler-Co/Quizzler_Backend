# LeaderBoard Service

The **LeaderBoard Service** tracks and displays user rankings based on quiz performance. It aggregates scores, manages leaderboards, and provides APIs for retrieving rankings and statistics.

## Features

- Maintain and update user scores
- Generate leaderboards by quiz, category, or globally
- RESTful API endpoints
- Integration with Quiz and User services
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

| Method | Endpoint            | Description             |
| ------ | ------------------- | ----------------------- |
| GET    | /leaderboard        | Get global leaderboard  |
| GET    | /leaderboard/{quiz} | Get leaderboard by quiz |

## Project Structure

```
LeaderBoard/
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
