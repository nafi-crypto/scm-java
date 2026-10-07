FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY backend backend
ARG SERVICE
RUN test -n "$SERVICE" && mvn -B -ntp -pl "backend/$SERVICE" -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system scm && useradd --system --gid scm --home-dir /app scm
ARG SERVICE
COPY --from=build --chown=scm:scm "/workspace/backend/${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar" /app/app.jar
USER scm
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
