# Используем официальный образ с JDK 17 и Gradle (для сборки)
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /app

# Копируем файлы проекта (включая gradlew и gradle/)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Делаем gradlew исполняемым
RUN chmod +x ./gradlew

# Собираем fat jar
RUN ./gradlew bootJar --no-daemon

# Второй этап — runtime
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Копируем собранный jar из builder-а
COPY --from=builder /app/build/libs/*.jar app.jar

# Открываем порт (если нужно)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
