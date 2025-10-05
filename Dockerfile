#builder stage
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY .mvn/ .mvn

COPY mvnw pom.xml ./

RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline

#CODE BUILD
COPY src ./src

RUN ./mvnw package -DskipTests

#runtime stage
FROM eclipse-temurin:21-jre-jammy

RUN useradd -ms /bin/bash springuser
USER springuser

WORKDIR /home/springuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# docker build -t hangman .
