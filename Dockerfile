# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/brokerage-firm-backend-0.0.1-SNAPSHOT.jar brokerage-firm-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "brokerage-firm-backend.jar"]