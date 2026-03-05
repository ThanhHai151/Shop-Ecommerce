FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper and project files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (improves build caching)
RUN ./mvnw -q -B dependency:go-offline

# Copy source code and build the application (skip tests for faster image build)
COPY src src
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built WAR file from the build stage
COPY --from=build /app/target/main-0.0.1-SNAPSHOT.war app.war

# Expose the application port
EXPOSE 2345

# Use environment variables to allow overriding DB connection when running in Docker
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.war"]

