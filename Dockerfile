FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar mancala.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "mancala.jar"]