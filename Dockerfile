# Backend Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Kopiranje aplikacije
COPY /build/libs/eScheduler-*.jar app.jar


# Port na kojem Spring Boot aplikacija radi
EXPOSE 2525

# Komanda za pokretanje aplikacije
ENTRYPOINT ["java", "-jar", "app.jar"]