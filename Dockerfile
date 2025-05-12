FROM openjdk:22-jdk-slim
ARG JAR_FILE=target/*.jar
WORKDIR /app
COPY ./target/healthapi-0.0.1-SNAPSHOT.jar /app/health-api.jar
ENTRYPOINT ["java", "-jar", "health-api.jar"]