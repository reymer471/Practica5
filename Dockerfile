
FROM gradle:9.2-jdk25 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean shadowJar --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]