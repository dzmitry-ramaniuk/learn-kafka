FROM gradle:8.8.0-jdk21 AS builder

WORKDIR /app

COPY build.gradle /app
COPY settings.gradle /app
COPY src /app/src

RUN gradle build --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]