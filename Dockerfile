
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/trabajo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]