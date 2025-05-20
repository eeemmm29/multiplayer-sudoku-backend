# Stage 1: Build the application using Maven
FROM maven:3.9.9-eclipse-temurin-24 AS builder
WORKDIR /app

# Copy source code and build
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with a slim JVM image
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]