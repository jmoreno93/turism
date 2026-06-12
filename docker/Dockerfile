# syntax=docker/dockerfile:1.7

# Build stage: compila el backend Spring Boot y empaqueta también el frontend estático.
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

# Primero copiamos el pom para aprovechar caché de dependencias.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -DskipTests dependency:go-offline

# Luego copiamos el código completo.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -DskipTests package

# Runtime stage: imagen final ligera para ejecutar el JAR.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S turism && adduser -S turism -G turism \
    && mkdir -p /app/logs /tmp/turism-logs \
    && chown -R turism:turism /app /tmp/turism-logs

COPY --from=build /workspace/target/*.jar /app/app.jar

RUN chown turism:turism /app/app.jar

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080
ENV LOG_PATH=/tmp/turism-logs
ENV LOG_LEVEL=INFO

EXPOSE 8080

USER turism:turism

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
