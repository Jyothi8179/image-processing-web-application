# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim


ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies for building ImageMagick and downloading it
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget \
    build-essential \
    pkg-config \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libwebp-dev \
    libx11-dev \
    libfreetype6-dev \
    liblcms2-dev \
    libxml2-dev \
    libraw-dev \
    libtool \
    libltdl-dev \
    && rm -rf /var/lib/apt/lists/*

# Install ImageMagick 7.1.1-47 from source
RUN wget https://download.imagemagick.org/archive/ImageMagick-7.1.1-47.tar.gz && \
    tar xvzf ImageMagick-7.1.1-47.tar.gz && \
    cd ImageMagick-7.1.1-47 && \
    ./configure && make && make install && ldconfig && \
    cd .. && rm -rf ImageMagick-7.1.1-47*

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR to the container
COPY app/target/app-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
