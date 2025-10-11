# Stage 1: Build frontend
FROM node:18 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM gradle:8.2.0-jdk17 AS backend-build
WORKDIR /app
COPY holdings-analyser/ holdings-analyser/
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle/ gradle/
# Copy frontend build to backend resources
COPY --from=frontend-build /app/frontend/dist holdings-analyser/src/main/resources/static
RUN ./gradlew :holdings-analyser:build --no-daemon

# Stage 3: Run
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-build /app/holdings-analyser/build/libs/holdings-analyser-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
