FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the executable Spring Boot jar produced by the Gradle bootJar task.
COPY build/libs/api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--server.port=8080"]

