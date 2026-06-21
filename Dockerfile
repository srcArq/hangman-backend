# Stage 1: Build con Maven + JDK 21
FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src

# Compila el jar (sin tests para acelerar; quita -DskipTests si quieres ejecutarlos)
RUN mvn clean package -DskipTests

# Stage 2: Runtime con JRE 21 (imagen más pequeña que el JDK)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Ejecutar como usuario no-root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown app:app app.jar
USER app

ENV PORT=8080
EXPOSE 8080

# Health check contra el endpoint de Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
