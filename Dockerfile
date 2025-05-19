# Сборочный этап
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew

# Сборка и логирование
RUN ./gradlew bootJar --no-daemon && \
    echo "=== build/libs ===" && \
    ls -lh build/libs

# Runtime
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
