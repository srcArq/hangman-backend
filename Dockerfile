# Stage 1: Build con Maven + JDK 21
FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src

# Compila el jar (sin tests para acelerar; quita -DskipTests si quieres ejecutarlos)
RUN mvn clean package -DskipTests

# Stage 2: Runtime con JDK 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

ENV PORT 8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
