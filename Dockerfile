# Stage 1: Build frontend
FROM node:20-alpine AS frontend-build

WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM gradle:8.5-jdk17 AS backend-build

WORKDIR /app
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY holdings-analyser holdings-analyser
# Ensure frontend is available for Gradle npm tasks (some builds expect /app/frontend)
COPY frontend /app/frontend
RUN mkdir -p holdings-analyser/src/main/resources/static
COPY --from=frontend-build /frontend/dist holdings-analyser/src/main/resources/static

# Build backend
WORKDIR /app/holdings-analyser
RUN gradle clean build -PskipFrontend

# Stage 3: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Install Python 3 and pip
RUN apk add --no-cache python3 py3-pip

# Copy Spring Boot JAR
COPY --from=backend-build /app/holdings-analyser/build/libs/holdings-analyser-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
