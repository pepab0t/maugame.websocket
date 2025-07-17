FROM openjdk:21-oracle
LABEL authors="josefcernik"

WORKDIR /app

COPY ./target/websocket-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar","./websocket-0.0.1-SNAPSHOT.jar"]