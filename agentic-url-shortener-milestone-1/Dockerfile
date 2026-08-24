FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn --batch-mode --no-transfer-progress clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/agentic-url-shortener-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
