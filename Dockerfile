FROM alpine/java:21-jre

LABEL authors="josefcernik"

WORKDIR /app

ADD ./target/maugame-server-1.0.0-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar","./websocket-0.0.1-SNAPSHOT.jar"]