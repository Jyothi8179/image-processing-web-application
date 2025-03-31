# Dockerfile for backend service
FROM openjdk:17-jdk-slim
WORKDIR /resize


# Set environment variables
ENV UPLOAD_DIR=/resize/uploads
ENV RESIZED_DIR=/resize/resized

# Create necessary directories
RUN mkdir -p $UPLOAD_DIR $RESIZED_DIR

# Install required dependencies, including ImageMagick
RUN apt-get update && apt-get install -y imagemagick && rm -rf /var/lib/apt/lists/*


COPY ./target/backend-0.0.1-SNAPSHOT.jar image_processor.jar
COPY ./src/main/resources/test.jpeg test.jpeg

ENTRYPOINT ["java", "-jar", "image_processor.jar"]

