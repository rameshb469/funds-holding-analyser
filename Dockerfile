# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle files and child project
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY frontend frontend
COPY holdings-analyser holdings-analyser

# Build React frontend first (Node plugin)
WORKDIR /app/frontend
RUN npm install
RUN npm run build

# Copy React build to Spring Boot static resources in child project
RUN mkdir -p /app/holdings-analyser/src/main/resources/static
RUN cp -r build/* /app/holdings-analyser/src/main/resources/static/

# Back to child project directory
WORKDIR /app/holdings-analyser

# Build the Spring Boot JAR for child project
RUN gradle clean bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy built JAR from child project
COPY --from=build /app/holdings-analyser/build/libs/holdings-analyser-1.0-SNAPSHOT.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
