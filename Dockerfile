# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR to the container
COPY app/target/app-0.0.1-SNAPSHOT.jar app_v1.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app_v1.jar"]
