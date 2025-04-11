# syntax=docker/dockerfile:1.4

# Use OpenJDK 17 slim image and ensure platform compatibility
FROM --platform=linux/amd64 openjdk:17-jdk-slim


ENV DEBIAN_FRONTEND=noninteractive

# Use secure sources and install dependencies
RUN sed -i 's|http://deb.debian.org|https://deb.debian.org|g; s|http://security.debian.org|https://security.debian.org|g' /etc/apt/sources.list && \
    apt-get update && \
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

# Set working directory
WORKDIR /app

# Copy the JAR
COPY app/target/app-0.0.1.jar app.jar

EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
