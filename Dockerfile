FROM openjdk:8
RUN mkdir /app
WORKDIR /app

COPY resources/logback-docker.xml /app
COPY target/bts.jar /app

CMD ["java", "-Dlogback.configurationFile=/app/logback-docker.xml", "-jar", "/app/bts.jar"]
EXPOSE 8080