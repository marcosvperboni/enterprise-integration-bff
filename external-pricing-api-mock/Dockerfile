FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/external-pricing-api-mock.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
