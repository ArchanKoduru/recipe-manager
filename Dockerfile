# Use OpenJDK 17 image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/recipe-manager-0.1.0.jar app.jar
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh


EXPOSE 8080

ENTRYPOINT ["./wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]
