FROM openjdk:8
RUN mkdir /app
WORKDIR /app

COPY target/bts.jar /app

CMD ["java", "-jar", "/app/bts.jar"]
EXPOSE 8080