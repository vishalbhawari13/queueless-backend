# Use Java 17
FROM eclipse-temurin:17-jdk

# App directory
WORKDIR /app

# Copy Maven wrapper FIRST
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give execute permission (CRITICAL)
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the jar (use exact jar name)
CMD ["java", "-jar", "target/queueless-backend-0.0.1-SNAPSHOT.jar"]
