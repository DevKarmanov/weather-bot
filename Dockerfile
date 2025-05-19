# Этап 1: сборка
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Этап 2: запуск
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/weather-model-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
