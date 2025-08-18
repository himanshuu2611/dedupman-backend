# ======================
# 1. Build stage
# ======================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy source code
COPY . .

# Build project without tests
RUN mvn clean package -DskipTests

# ======================
# 2. Runtime stage
# ======================
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy only the built jar from build stage
COPY --from=build /app/target/dedupman-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
