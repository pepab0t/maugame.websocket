FROM alpine/java:21-jre

LABEL authors="josefcernik"

WORKDIR /app

ADD ./target/websocket-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar","./websocket-0.0.1-SNAPSHOT.jar"]