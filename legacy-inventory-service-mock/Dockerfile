FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/legacy-inventory-service-mock.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
