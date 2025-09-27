# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code
COPY . .

# Build React frontend first (assuming it's in /frontend)
WORKDIR /app/frontend
RUN npm install
RUN npm run build

# Copy React build into Spring Boot static resources
RUN mkdir -p /app/backend/src/main/resources/static
RUN cp -r build/* /app/backend/src/main/resources/static/

# Back to Spring Boot directory
WORKDIR /app/backend

# Build Spring Boot JAR
RUN gradle clean bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/backend/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]

# Start from a base image
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies first for better caching
COPY package*.json ./
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application (if applicable)
# RUN npm run build

# Production image
FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app ./

EXPOSE 3000

CMD ["npm", "start"]
