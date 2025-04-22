# syntax=docker/dockerfile:1.4

# Use OpenJDK 17 slim image and ensure platform compatibility
FROM --platform=linux/amd64 openjdk:17-jdk-slim


ENV DEBIAN_FRONTEND=noninteractive
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-cvrm3v6r433s73augu10-a.oregon-postgres.render.com:5432/image_resize_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=tPSopK24ar64tqyTv3aJukunUzwHcYXS
ENV SERVER_PORT=10000
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
ENV SELF_PING_URL=https://image-processing-web-application.onrender.com/health
ENV CLEANUP_INTERVAL_MILISECONDS=21600000
ENV CLEANUP_TOKEN=myCustomCleanUpToken


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

# Set working- directory
WORKDIR /app

# Copy  JAR
COPY app/target/app-0.0.1.jar app-v1.jar
# PORT for render
EXPOSE ${PORT}

# Run the Spring-Boot app
ENTRYPOINT ["java", "-jar", "app-v1.jar"]
