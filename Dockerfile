# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and project files
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY frontend frontend
COPY backend backend

# Build React frontend using Gradle + Node plugin
WORKDIR /app/backend
RUN gradle clean copyFrontendBuild --no-daemon

# Build Spring Boot JAR with explicit name
RUN gradle bootJar --no-daemon -PbootJar.archiveBaseName=holdings-analyser

# Stage 2: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/backend/build/libs/holdings-analyser-1.0-SNAPSHOT.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
