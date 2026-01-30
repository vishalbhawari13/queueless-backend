# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN ./mvnw dependency:go-offline || true

# Copy source code
COPY src ./src
COPY mvnw .
COPY .mvn .mvn

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Render uses dynamic port)
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
