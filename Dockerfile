FROM eclipse-temurin:17-jdk
VOLUME /tmp
COPY observability/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]