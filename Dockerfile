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

# Build Spring Boot JAR
RUN gradle bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the Spring Boot JAR from build stage
COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
