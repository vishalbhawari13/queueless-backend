# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and config FIRST
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give execute permission to mvnw (CRITICAL)
RUN chmod +x mvnw

# Download dependencies (faster builds)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Render uses dynamic port)
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
