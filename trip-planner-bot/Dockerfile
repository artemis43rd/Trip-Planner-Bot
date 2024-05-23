FROM openjdk:21-jdk-slim
ARG JAR_FILE=target/*.jar
COPY target/trip-planner-bot-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]