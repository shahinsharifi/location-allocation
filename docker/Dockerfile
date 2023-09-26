# Use the official maven/Java 17 image to create a build artifact.
FROM maven:3.8-jdk-17 as builder

# Set the current working directory inside the image
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# This command will download all required dependencies into the maven repository
# We do this first to reuse these layers for each build
RUN ./mvnw dependency:go-offline -B

# Copy your other files
COPY src src

# Build the project
RUN ./mvnw clean package

# Start with a base image containing Java runtime
FROM openjdk:17-jdk-slim

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Set application's JAR file
ARG JAR_FILE=target/*.jar

# Add the application's JAR file to the container
COPY --from=builder /app/${JAR_FILE} app.jar

# Run the JAR file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
