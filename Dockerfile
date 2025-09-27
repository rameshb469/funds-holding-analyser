# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper + build files
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY frontend frontend
COPY holdings-analyser holdings-analyser

# Build frontend + backend
WORKDIR /app/holdings-analyser
RUN gradle clean build

# Stage 2: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Spring Boot JAR
COPY --from=build /app/holdings-analyser/build/libs/holdings-analyser-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
