
### 📦 Build the Project
Run this command in the terminal:

```shell 
  mvn clean install
```

---

### 🐳 Run Project

#### 1. Rebuild docker container ( without CACHE )
```shell
  docker-compose build --no-cache
```
#### 2. Rebuild with cache ( faster )
```shell
  docker-compose build
```

#### 2. Rebuild & Run ( all containers )
```shell
  docker-compose up --build
```
---

<!--
#### 3. Re-Run
```shell
  mvn spring-boot:run 
```

-->

---

### 📚 Misclenious Docker Commands
#### 1. Build Docker Image
To create a Docker image of your Spring Boot application, run:
``` shell
  docker build -t image-processing-app .
  docker build -t image-resize-backend .
```

#### 2. Verify Running Containers
Check running containers with:
```shell
  docker ps
```

#### 3. Access to docker bash shell
```shell
  docker exec -it image-resize-backend bash
```
#### 4. Build and Run All Containers
To build and run Spring Boot and MySQL together using docker-compose.yml:
```shell
  docker-compose up --build
```

#### 5. Check Docker Logs
To view logs from the application container:
```shell
  docker logs image-processing-app
```

#### 6. Stop and Clean Containers
To stop the containers:
```shell
  docker-compose down
```
---

#### 7. Remove Docker Images and Containers
If you need to clean up Docker resources:
```shell
  docker system prune -a
```

### 🚀 Access the Application
- Open your browser and navigate to:
```
http://localhost:8080
```
- Database is accessible at:
```
localhost:3306
```

### Build and push docker image to dockerHub
```shell
   docker build --platform linux/amd64 --no-cache -t jyothi8179/image-processing-web-application .
```
