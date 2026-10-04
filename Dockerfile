FROM gradle:8.8-jdk21 AS builder

WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN gradle bootJar --no-daemon \
    && find build/libs -name '*-plain.jar' -delete

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
