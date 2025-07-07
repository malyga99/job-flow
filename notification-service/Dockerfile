#FROM maven:latest AS builder
#WORKDIR /app
#COPY . .
#RUN mvn clean package -DskipTests
#
#FROM openjdk:latest
#WORKDIR /app
#COPY --from=builder /app/target/notification-service-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM openjdk:latest
WORKDIR /app
COPY /target/notification-service-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]