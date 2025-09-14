# Common Shared Library Build Container
# This Dockerfile is used to build the common library JAR for use in other services
FROM openjdk:24-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the library (install to local repo and create JAR)
RUN ./mvnw clean install -DskipTests -B

# Copy the built JAR to a well-known location for easy extraction
RUN cp target/*.jar /app/common-library.jar

# This image is used to build the common library
# The JAR can be extracted using: docker cp <container>:/app/common-library.jar ./
# Or used in multi-stage builds in other services

# Default command shows the built JAR location
CMD ["ls", "-la", "/app/common-library.jar"]
