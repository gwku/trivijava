# TriviJava: Building a Quiz App with Java

A simple quiz web application backend built with Java 21 and Spring Boot, which fetches trivia questions from the Open Trivia Database API and allows users to answer them without exposing the correct answers on the client side.

You can find a hosted version here: [Trivijava](https://trivijava.gkloud.nl).

To read about the building process, you can read my [blog post](https://gerwinkuijntjes.nl/en/projects/building-a-quiz-app-with-java).

---

## Introduction

After extensive experience building .NET APIs, I decided to challenge myself by trying out Java Spring Boot — a technology often seen as outdated by some, but loved by many Java developers. My goal was to see how Spring Boot compares, especially for building REST APIs with familiar architectural patterns like controllers, services, and repositories.

This project is a practical exploration based on real-world usage, using the "package by layer" approach to structure the code.

---

## Project Overview

The assignment was to create a simple quiz app where users can answer trivia questions retrieved from the Open Trivia Database (OpenTDB) API. The backend is responsible for:

- Fetching trivia questions from OpenTDB.
- Ensuring the correct answers are not exposed to the client (to prevent cheating).
- Validating users' answers on the server side.
- Providing a simple REST API.
- Serving a basic frontend to interact with the quiz.

A database is not required for this demo; all data is temporarily stored in memory.

---

## Features & Design Highlights

- **Backend:** Java 21 + Spring Boot with RESTful endpoints.
- **Endpoints:**
    - `GET /api/v1/quiz/questions` — Retrieve a set of trivia questions.
    - `POST /api/v1/quiz/answers` — Submit answers and get results.
- **Session handling:** Uses an `X-Session-Id` header to track user sessions and tokens from OpenTDB to ensure unique questions.
- **Answer hiding:** Correct answers are never sent to the client; validation happens on the backend.
- **In-memory storage:** Uses a simple `Map<UUID, UUID>` to store correct answers per question during a session.
- **Rate limiting and token management:** Handles OpenTDB’s token lifecycle and API rate limits.
- **Frontend:** Minimal HTML, CSS, and JS served from `src/main/resources/static/index.html`.

---

## API Usage

OpenAPI documentation is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

### Get Questions

```http
GET /api/v1/quiz/questions
Headers:
  X-Session-Id: {sessionId}
Query Parameters:
  amount=10  (default, number of questions to fetch)

Returns a list of questions without correct answers.
```

### Submit Answers

```http
POST /api/v1/quiz/answers
Content-Type: application/json

{
  "answers": [
    { "questionId": "uuid", "answerId": "uuid" },
    ...
  ]
}

Returns a result indicating which answers were correct.
```

**Implementation Details**

- Controller handles request validation and DTO conversion.
- Service manages token retrieval, question fetching, and answer validation.
- Repository stores correct answers temporarily in memory.
- TriviaApiClient interacts with the OpenTDB API, managing tokens and handling response codes.
- Uses modern Java features like records, Optionals, and streams for concise and clear code.
- Follows Spring Boot idiomatic practices including Dependency Injection and annotation-driven configuration.

## Running the Application
To run the application, ensure you have Java 21 and Maven installed. Then, execute the following commands:

Clone the repository:
```bash
git clone https://github.com/gwku/trivijava.git
cd trivijava
```

Build and run the application:
```bash
./mvnw spring-boot:run
```

Open your browser and navigate to `http://localhost:8080` to access the quiz app.

## Testing
To run the tests, use the following command:

```bash
./mvnw test
```

This will execute all unit tests defined in the project.

## Deployment with Docker
To build and run the application in a Docker container, use the following commands:

```bash
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=ghcr.io/gwku/trivijava
docker run -p 8080:8080 ghcr.io/gwku/trivijava
```

Example Docker Compose file (`docker-compose.yml`):

```yaml
services:
  trivijava:
    image: ghcr.io/gwku/trivijava:latest
    environment:
      SPRING_PROFILES_ACTIVE: "prod"
    restart: unless-stopped
```
