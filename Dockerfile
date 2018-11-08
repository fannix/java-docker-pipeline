FROM openjdk:8-jre-alpine

COPY ./target/pipeline-1.0-SNAPSHOT-jar-with-dependencies.jar /app.jar
