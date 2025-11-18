# Online Judge Backend

A minimal Java-based online judge backend using Spring Boot and Docker.

## Features

- `/api/submit` endpoint that accepts `problemId` and `code`
- Docker-based compilation and execution (no code runs in JVM)
- Problem templates and test cases
- JSON verdict responses

## Requirements

- Java 17+
- Maven
- Docker

## Running

```bash
mvn spring-boot:run
```

## API Usage

```bash
POST /api/submit
Content-Type: application/json

{
  "problemId": "example1",
  "code": "return input;"
}
```

## Response Format

```json
{
  "verdict": "ACCEPTED|WRONG_ANSWER|COMPILATION_ERROR|RUNTIME_ERROR|ERROR",
  "message": "Description",
  "expectedOutput": "...",
  "actualOutput": "..."
}
```

## Project Structure

```
src/main/resources/
  judge/
    templates/
      RunnerTemplate.java
    problems/
      {problemId}/
        input.txt
        expected.txt
```

