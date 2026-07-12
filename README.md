# Ahorcado Backend

API REST del juego del Ahorcado, construida con Spring Boot, Java 21 y MongoDB.

---

## Funcionalidades

- Spring Boot 3.5.3 sobre Java 21.
- Partidas sin sesión de servidor: el estado viaja cifrado con AES-GCM.
- La palabra nunca se revela mientras la partida está en curso.
- Cuatro dificultades, incluido el modo Extremo con tiempo controlado por el servidor.
- Castellano, catalán e inglés con **827 palabras** incluidas en recursos.
- Catálogo de palabras sincronizado con MongoDB y caché en memoria.
- Fallback automático a recursos si Mongo no está disponible para seleccionar palabras.
- Ranking global top 5 persistente en MongoDB.
- Verificación del token ganador y deduplicación por partida para impedir sumar una victoria varias veces.
- Validación de entrada, CORS configurable y health check de Actuator.
- Imagen Docker multi-stage con JRE 21 y usuario sin privilegios.

---

## Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.3 (`spring-boot-starter-web`, `-validation`, `-actuator`, `-data-mongodb`)
- **Database:** MongoDB Atlas en producción; recursos locales como fallback del catálogo
- **Build Tool:** Maven (artifact `hangman-0.0.1-SNAPSHOT.jar`)
- **Containerization:** Docker multi-stage build — builder `eclipse-temurin:21-jdk-jammy`, runtime `eclipse-temurin:21-jre-jammy` (Temurin JRE on Ubuntu Jammy)

---

## API REST

Todos los endpoints se sirven bajo el prefijo `/api` definido en `HangmanController`.

| Method | Path                | Description                                                                 |
| ------ | ------------------- | --------------------------------------------------------------------------- |
| `POST` | `/api/selectLevel`  | Inicia una partida. Body: `{ "level": 1-4, "language": "ES" \| "CA" \| "EN" }` (`language` es opcional y por defecto usa `ES`). Devuelve el estado con un `token` cifrado. |
| `POST` | `/api/checkLetter`  | Juega una letra. Body: `{ "token": "...", "letter": "A-Z o Ñ" }`. Devuelve el estado actualizado. |
| `POST` | `/api/score`        | Registra una victoria verificada y deduplicada. Body: `{ "name": "...", "token": "..." }`. Devuelve el ranking actual. |
| `GET`  | `/api/ranking`      | Devuelve el top 5 de jugadores por número de victorias.                     |

La respuesta de juego contiene `token`, `maskedWord`, `attempts`,
`guessedLetters`, `wordLength`, `status`, `resultWord` y
`remainingTimeMillis`. `resultWord` solo contiene la palabra cuando el estado es
`WON` o `LOST`.

---

## Configuración

La configuración vive en `src/main/resources/application.properties`. Estas variables de entorno sobrescriben los valores de desarrollo y **deben configurarse en producción**:

| Variable                | Valor por defecto (solo desarrollo)                                                | Uso                                                                    |
| ----------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| `MONGODB_URI`           | `mongodb://localhost:27017/hangman?serverSelectionTimeoutMS=800`                   | URI de conexión a MongoDB (MongoDB Atlas en producción).               |
| `HANGMAN_TOKEN_SECRET`  | `dev-only-insecure-secret-change-me`                                               | Secreto usado para cifrar el token de partida con AES-GCM. **Debe ser largo y aleatorio en producción.** |
| `HANGMAN_CORS_ORIGINS`  | `http://localhost:4321,https://hangman-frontend-sable.vercel.app`                  | Lista de orígenes CORS permitidos, separada por comas.                 |
| `PORT`                  | `8080`                                                                             | Puerto HTTP en el que escucha el servidor.                             |

> Si MongoDB no responde, el juego sigue seleccionando palabras desde los
> recursos incluidos. El ranking y la sincronización persistente del catálogo sí
> requieren MongoDB. El indicador Mongo de Actuator está desactivado para que
> una caída de la base de datos no marque como caído todo el servicio.

---

## Desarrollo local

### Requisitos

- Java 21 JDK.
- Maven 3.x, o el Maven Wrapper incluido (`./mvnw`).
- Docker, si quieres construir o ejecutar el contenedor.
- MongoDB opcional para jugar en local; necesario para ranking persistente.

### Clonar y ejecutar en local

```bash
git clone https://github.com/srcArq/hangman-backend.git
cd hangman-backend

# Genera el JAR; quita -DskipTests si quieres ejecutar los tests
./mvnw clean package

# Ejecuta la aplicación
java -jar target/hangman-0.0.1-SNAPSHOT.jar
```

La aplicación arranca por defecto en el puerto `8080`. Health check: `http://localhost:8080/actuator/health`.

### Ejecutar con Docker

```bash
# Construye la imagen multi-stage
docker build -t hangman-backend .

# Ejecuta el contenedor con las variables necesarias
docker run -p 8080:8080 \
  -e MONGODB_URI="<your-mongodb-uri>" \
  -e HANGMAN_TOKEN_SECRET="<your-random-secret>" \
  -e HANGMAN_CORS_ORIGINS="https://your-frontend.example.com" \
  hangman-backend
```

---

## Despliegue

- Producción principal: [Render](https://hangman-backend-1.onrender.com), desplegado desde `main`.
- `.github/workflows/keep-alive.yml` consulta `/actuator/health` cada 10 minutos mediante un cron best-effort.
- `cloudbuild.yaml` mantiene una ruta alternativa: construye la imagen, la publica en Artifact Registry y despliega `hangmanbackend` en Cloud Run (`us-central1`).

Recuerda configurar `MONGODB_URI`, `HANGMAN_TOKEN_SECRET` y `HANGMAN_CORS_ORIGINS` en la plataforma donde despliegues.
