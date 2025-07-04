#1: Build con Maven + JDK 21
FROM maven:3.9.0-openjdk-21 AS build
WORKDIR /workspace

# Copiamos solo pom.xml primero para cacheo de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Ahora copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución con JRE ligero
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el .jar compilado
COPY --from=build /workspace/target/*.jar app.jar

# Expone el puerto estándar
EXPOSE 8080

# Solo usamos ENTRYPOINT limpio
ENTRYPOINT ["java", "-jar", "app.jar"]

