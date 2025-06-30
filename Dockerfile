# Etapa de construcción: usamos la imagen oficial de Maven con JDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos todos los ficheros y compilamos sin tests para acelerar
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución: usamos un JRE ligero
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiamos el JAR compilado desde la fase de build
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto que configura Spring Boot (8080)
EXPOSE 8080

# Comando por defecto al arrancar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
