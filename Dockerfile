# Stage 1: build the app
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the Maven metadata first for better caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# Copy source code
COPY src src

# Build the executable jar
RUN ./mvnw -q -DskipTests clean package

# Stage 2: runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]