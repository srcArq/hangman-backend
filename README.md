# Hangman Backend

_A RESTful backend for the Hangman game, built with Spring Boot and MongoDB,
Docker‑enabled for easy deployment. It is the server half of a two‑repo product;
the client is the [hangman-frontend](https://github.com/srcArq/hangman-frontend)
(Astro + React)._

---

## 💻 Features

- **Spring Boot 3.5.3** application running on **Java 21**
- **Maven**‑driven build and dependency management (Maven Wrapper included)
- **MongoDB** (Spring Data MongoDB) for the persistent leaderboard / ranking
- **Stateless gameplay**: the whole game state travels in an **encrypted token**
  on every request; only the ranking is persisted
- **Spring Boot Actuator** health endpoint (`/actuator/health`)
- **Docker** multi‑stage build for a lean runtime image
- **JUnit 5** & **Spring Boot Test** for unit and integration testing

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.3 (`spring-boot-starter-web`, `-validation`, `-actuator`, `-data-mongodb`)
- **Database:** MongoDB (MongoDB Atlas in production)
- **Build Tool:** Maven (artifact `hangman-0.0.1-SNAPSHOT.jar`)
- **Containerization:** Docker multi‑stage build — builder `eclipse-temurin:21-jdk-jammy`, runtime `eclipse-temurin:21-jre-jammy`

---

## 🌐 REST API

All endpoints are served under the `/api` base path (see `HangmanController`).

| Method | Path                | Description                                                                 |
| ------ | ------------------- | --------------------------------------------------------------------------- |
| `POST` | `/api/selectLevel`  | Starts a new game. Body: `{ "level": 1-4, "language": "ES" \| "CA" \| "EN" }` (language optional, defaults to `ES`). Returns the game state with an encrypted `token`. |
| `POST` | `/api/checkLetter`  | Plays a letter on an existing game. Body: `{ "token": "...", "letter": "A-Z or Ñ" }`. Returns the updated game state. |
| `POST` | `/api/score`        | Records a win in the ranking (the server verifies the winning token). Body: `{ "name": "...", "token": "..." }`. Returns the current ranking. |
| `GET`  | `/api/ranking`      | Returns the top 5 players by number of wins.                                |

The game response (`GameResponse`) includes the (re‑encrypted) `token`, the
`maskedWord`, remaining `attempts`, `guessedLetters`, `wordLength`, `status`
(`IN_PROGRESS` \| `WON` \| `LOST`) and — only when the game has finished — the
revealed `resultWord`.

**Attempts per level** (`Difficulty`): `1` easy → **10**, `2` medium → **7**,
`3` hard → **5**, `4` extreme → **99**. Level 4 keeps a high attempt count on
purpose: the real limit for *Extreme* mode is **time**, enforced by the client;
wrong letters cost time rather than ending the game on the server.

### Error contract

Domain and validation errors return a uniform JSON body
`{"error": "...", "message": "..."}` via `GlobalExceptionHandler`:

| Status | `error` code    | When                                                             |
| ------ | --------------- | ---------------------------------------------------------------- |
| `400`  | `INVALID_INPUT` | Invalid body: level outside 1–4, malformed letter, or scoring a game that is not won. |
| `400`  | `INVALID_TOKEN` | Token missing, malformed, or tampered (AES‑GCM tag mismatch).    |
| `409`  | `GAME_OVER`     | Playing a letter on a game that has already finished.            |

---

## 🔒 Security & anti‑abuse

Verified against the code (and the test suite) in this repo:

- **Encrypted, tamper‑evident tokens.** The game state is sealed with **AES‑GCM**
  (`GameCodec`). The secret word is never sent to the client in plaintext, and a
  modified token fails the GCM integrity check and is rejected with `INVALID_TOKEN`.
- **Server‑verified wins.** `/api/score` decrypts the submitted token and only
  counts it if `status == WON`; otherwise it responds `400 INVALID_INPUT`
  (`"Solo se puntúan partidas ganadas"`). A client cannot inflate the ranking
  with an arbitrary request.
- **Replay protection.** Each counted win stores the **SHA‑256 hash** of its
  winning token (`recordedGames` collection, unique `_id`); re‑submitting the
  same token does not add a second win.
- **Restricted CORS.** `CorsConfig` allows only the configured origins
  (`HANGMAN_CORS_ORIGINS`) and only the `GET`/`POST` methods — no `@CrossOrigin("*")`.
- **Input validation.** Jakarta Validation constrains the level (`1–4`), the
  letter (a single `[A-Za-zÑñ]`) and the player name (`≤ 20` chars, restricted
  character set).
- **Secret via environment.** `HANGMAN_TOKEN_SECRET` is injected from the
  environment; the in‑repo default is clearly marked as development‑only.

No secrets are committed to the repository; the values above are configured
through environment variables (see below).

---

## ⚙️ Configuration & Environment Variables

Configuration lives in `src/main/resources/application.properties`. The following
environment variables override the development defaults and **should be set in
production**:

| Variable                | Default (dev only)                                                                 | Purpose                                                                 |
| ----------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| `MONGODB_URI`           | `mongodb://localhost:27017/hangman?serverSelectionTimeoutMS=800`                   | MongoDB connection URI (MongoDB Atlas in production).                  |
| `HANGMAN_TOKEN_SECRET`  | `dev-only-insecure-secret-change-me`                                               | Secret used to encrypt the game token (AES‑GCM). **Set a long, random value in production.** |
| `HANGMAN_CORS_ORIGINS`  | `http://localhost:4321,https://hangman-frontend-sable.vercel.app`                  | Comma‑separated list of CORS‑allowed origins.                          |
| `PORT`                  | `8080`                                                                             | HTTP port the server listens on.                                       |

### Words & MongoDB

Word lists are bundled as resources (`src/main/resources/words/<lang>_<level>.txt`
for `es`/`ca`/`en` and levels `1–4`). On startup `WordService` reconciles them
into MongoDB and caches them in memory.

**MongoDB degrades gracefully.** If Mongo is unreachable, the word source falls
back to the bundled resource files and the Mongo health indicator is disabled so
it does not fail the global health check. This was confirmed during the audit:
with no local Mongo, the app logged _"MongoDB no disponible … usando ficheros de
recurso"_ and `POST /api/selectLevel` / `POST /api/checkLetter` still worked.
MongoDB is only required for the **persistent ranking** — `POST /api/score` and
`GET /api/ranking` need the database.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK
- Maven 3.x (the bundled Maven Wrapper `./mvnw` can be used instead)
- Docker (only if you plan to build or run the container)
- MongoDB (optional for local play — see the note above; required for the ranking)

### Clone & Run Locally

```bash
git clone https://github.com/srcArq/hangman-backend.git
cd hangman-backend

# Build the JAR (skip tests for speed; remove -DskipTests to run them)
./mvnw clean package -DskipTests

# Run the application
java -jar target/hangman-0.0.1-SNAPSHOT.jar
```

The app starts on port `8080` by default. Health check:
`http://localhost:8080/actuator/health`.

Quick smoke test (no Mongo required for gameplay):

```bash
curl -s -X POST http://localhost:8080/api/selectLevel \
  -H 'Content-Type: application/json' -d '{"level":1,"language":"ES"}'
```

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

## ✅ Tests & Build

Verified locally during the audit (Java 21, Maven 3.9):

- `./mvnw test` → **26 tests passing** across 6 classes
  (`GameCodecTest`, `GameServiceTest`, `HangmanApplicationTests`,
  `HangmanControllerTest`, `LeaderboardServiceTest`, `WordServiceTest`).
  The Spring integration tests run **without** a MongoDB instance — the word
  source falls back to the bundled files.
- `./mvnw package -DskipTests` → builds `target/hangman-0.0.1-SNAPSHOT.jar`.

The tests cover the game rules (`GameService`), token encrypt/decrypt round‑trip
and tamper rejection (`GameCodec`), the ranking anti‑abuse rules
(`LeaderboardService`), the word loading/fallback (`WordService`) and the full
HTTP contract incl. error codes (`HangmanController`).

---

## ☁️ Deployment

- **Live deployment runs on [Render](https://render.com)** (free plan), reachable
  at `https://hangman-backend-1.onrender.com`. Because the free plan sleeps the
  service after ~15 min of inactivity, the GitHub Actions workflow
  `.github/workflows/keep-alive.yml` pings `/actuator/health` every ~10 minutes
  to keep it awake. On a cold start the health probe may briefly report
  `OUT_OF_SERVICE` (HTTP 503) before it returns `UP` once fully started.
- **Google Cloud Run is also supported** via `cloudbuild.yaml`: Cloud Build
  builds the Docker image, pushes it to Artifact Registry
  (`us-central1-docker.pkg.dev/$PROJECT_ID/hangman-backend-docker/mi-app`), and
  deploys the `hangmanbackend` service to Cloud Run in `us-central1`. This path
  is optional and triggered through Google Cloud Build.

Remember to configure `MONGODB_URI`, `HANGMAN_TOKEN_SECRET`, and
`HANGMAN_CORS_ORIGINS` in whichever platform you deploy to. The persistent
ranking only works when the deployment can reach its MongoDB instance.

---

## 🔗 Relationship with the frontend

The [hangman-frontend](https://github.com/srcArq/hangman-frontend) consumes the
four endpoints above via its `PUBLIC_API_URL` setting. Gameplay stays entirely
on the server (the browser only ever sees the masked word, attempts and status,
never the secret word), while the client owns presentation, local stats and the
Extreme‑mode timer.
