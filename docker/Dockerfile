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

RUN addgroup -S turism && adduser -S turism -G turism

COPY --from=build /workspace/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

EXPOSE 8080

USER turism:turism

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
