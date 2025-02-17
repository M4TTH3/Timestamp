FROM openjdk:21-jdk-slim AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradle.properties build.gradle.kts settings.gradle gradlew ./
COPY backend/src backend/src
COPY backend/build.gradle.kts backend/
COPY shared/src shared/src
COPY shared/build.gradle.kts shared/

COPY entrypoint.sh ./entrypoint.sh

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon :backend:bootJar

# Now create a minimal build with only the bootJar
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN mkdir osm
RUN mkdir graph-cache
COPY --from=build /app/backend/build/libs/timestamp.jar /app/timestamp.jar
COPY --from=build /app/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

CMD ["/app/entrypoint.sh"]
