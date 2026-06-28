# Hangman Backend

_A RESTful backend for the Hangman game, built with Spring Boot and MongoDB, Docker-enabled for easy deployment._

---

## 💻 Features

- **Spring Boot 3.5.3** application running on Java 21
- **Maven**-driven build and dependency management
- **MongoDB** (Spring Data MongoDB) for the persistent leaderboard / ranking
- **Stateless gameplay**: the whole game state travels in an encrypted token on every request; only the ranking is persisted
- **Spring Boot Actuator** health endpoint (`/actuator/health`)
- **Docker** multi-stage build for a lean runtime image
- **JUnit 5** & **Spring Boot Test** for unit and integration testing

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.3 (`spring-boot-starter-web`, `-validation`, `-actuator`, `-data-mongodb`)
- **Database:** MongoDB (MongoDB Atlas in production)
- **Build Tool:** Maven (artifact `hangman-0.0.1-SNAPSHOT.jar`)
- **Containerization:** Docker multi-stage build — builder `eclipse-temurin:21-jdk-jammy`, runtime `eclipse-temurin:21-jre-jammy` (Temurin JRE on Ubuntu Jammy)

---

## 🌐 REST API

All endpoints are served under the `/api` base path (see `HangmanController`).

| Method | Path                | Description                                                                 |
| ------ | ------------------- | --------------------------------------------------------------------------- |
| `POST` | `/api/selectLevel`  | Starts a new game. Body: `{ "level": 1-4, "language": "ES" \| "CA" \| "EN" }` (language optional, defaults to `ES`). Returns the game state with an encrypted `token`. |
| `POST` | `/api/checkLetter`  | Plays a letter on an existing game. Body: `{ "token": "...", "letter": "A-Z or Ñ" }`. Returns the updated game state. |
| `POST` | `/api/score`        | Records a win in the ranking (the server verifies the winning token). Body: `{ "name": "...", "token": "..." }`. Returns the current ranking. |
| `GET`  | `/api/ranking`      | Returns the top 5 players by number of wins.                                |

The game response includes the (re-encrypted) `token`, the masked word, remaining attempts, guessed letters, word length, status, and — only when the game has finished — the revealed word.

---

## ⚙️ Configuration & Environment Variables

Configuration lives in `src/main/resources/application.properties`. The following environment variables override the development defaults and **should be set in production**:

| Variable                | Default (dev only)                                                                 | Purpose                                                                 |
| ----------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| `MONGODB_URI`           | `mongodb://localhost:27017/hangman?serverSelectionTimeoutMS=800`                   | MongoDB connection URI (MongoDB Atlas in production).                  |
| `HANGMAN_TOKEN_SECRET`  | `dev-only-insecure-secret-change-me`                                               | Secret used to encrypt the game token (AES-GCM). **Set a long, random value in production.** |
| `HANGMAN_CORS_ORIGINS`  | `http://localhost:4321,https://hangman-frontend-sable.vercel.app`                  | Comma-separated list of CORS-allowed origins.                          |
| `PORT`                  | `8080`                                                                             | HTTP port the server listens on.                                       |

> **Note on MongoDB:** the game degrades gracefully if Mongo is unreachable — the word source falls back to bundled resources, and the Mongo health indicator is disabled so it does not bring down the global health check. MongoDB is only required for the persistent ranking. Running locally without a Mongo instance still lets you play; only `/api/score` and `/api/ranking` need the database.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK
- Maven 3.x (the bundled Maven Wrapper `./mvnw` can be used instead)
- Docker (if you plan to build or run the container)
- MongoDB (optional for local play — see the note above; required for the ranking)

### Clone & Run Locally

```bash
git clone https://github.com/srcArq/hangman-backend.git
cd hangman-backend

# Build the JAR (skip tests for speed; remove -DskipTests to run them)
mvn clean package -DskipTests

# Run the application
java -jar target/hangman-0.0.1-SNAPSHOT.jar
```

The app starts on port `8080` by default. Health check: `http://localhost:8080/actuator/health`.

### Run with Docker

```bash
# Build the image (multi-stage: builds the JAR, then runs it on a Temurin JRE)
docker build -t hangman-backend .

# Run, providing the required environment variables
docker run -p 8080:8080 \
  -e MONGODB_URI="<your-mongodb-uri>" \
  -e HANGMAN_TOKEN_SECRET="<your-random-secret>" \
  -e HANGMAN_CORS_ORIGINS="https://your-frontend.example.com" \
  hangman-backend
```

---

## ☁️ Deployment

- **Live deployment runs on [Render](https://render.com)** (free plan), reachable at `https://hangman-backend-1.onrender.com`. Because the free plan sleeps the service after ~15 min of inactivity, the GitHub Actions workflow `.github/workflows/keep-alive.yml` pings `/actuator/health` every ~10 minutes to keep it awake.
- **Google Cloud Run is also supported** via `cloudbuild.yaml`: Cloud Build builds the Docker image, pushes it to Artifact Registry (`us-central1-docker.pkg.dev/$PROJECT_ID/hangman-backend-docker/mi-app`), and deploys the `hangmanbackend` service to Cloud Run in `us-central1`. This path is optional and triggered through Google Cloud Build.

Remember to configure `MONGODB_URI`, `HANGMAN_TOKEN_SECRET`, and `HANGMAN_CORS_ORIGINS` in whichever platform you deploy to.
