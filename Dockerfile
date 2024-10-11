FROM openjdk:21-jdk-slim AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradle.properties build.gradle.kts settings.gradle gradlew ./
COPY backend/src backend/src
COPY backend/build.gradle.kts backend/

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon --refresh-dependencies :backend:dependencies
RUN ./gradlew --no-daemon :backend:classes

EXPOSE 8080

CMD ["./gradlew", ":backend:bootRun"]
