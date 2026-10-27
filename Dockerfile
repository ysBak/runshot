FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src
RUN bash gradlew test bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system runshot && useradd --system --gid runshot runshot
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar /app/runshot.jar
USER runshot
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/runshot.jar"]
