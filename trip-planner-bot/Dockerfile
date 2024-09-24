FROM openjdk:21-jdk-slim
ENV JAVA_OPTS=""
COPY ./target/trip_planner_bot-1.0-jar-with-dependencies.jar /app/trip_planner_bot-.jar
ENTRYPOINT ["java", "-cp", "/app/trip_planner_bot-.jar", "com.telegrambot.Main"]