# Hangman Backend

_A RESTful backend for the Hangman game, built with Spring Boot and Docker-enabled for easy deployment._

---

## 💻 Features

- **Spring Boot 3.5.3** application running on Java 21  
- **Maven**-driven build and dependency management  
- **Docker** multi-stage build for lean runtime image  
- **Cloud Build & Cloud Run** integration via `cloudbuild.yaml`  
- **Lombok** to reduce boilerplate code  
- **DevTools** for hot-reload during development  
- **JUnit 5** & **Spring Boot Test** for unit and integration testing  

---

## 🛠️ Tech Stack

- **Language:** Java 21  
- **Framework:** Spring Boot 3.5.3  
- **Build Tool:** Maven  
- **Containerization:** Docker (Alpine-based runtime)  
- **CI/CD:** Render → Cloud Run  
- **Codegen & Boilerplate:** Lombok  

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK  
- Maven 3.x  
- Docker (if you plan to build or run the container)  
  

### Clone & Run Locally

```bash
git clone https://github.com/srcArq/hangman-backend.git
cd hangman-backend

# Build the JAR (skip tests for speed; remove -DskipTests to run them)
mvn clean package -DskipTests

# Run the application
java -jar target/hangman-0.0.1-SNAPSHOT.jar
