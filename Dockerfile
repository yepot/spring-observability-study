FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY observability /workspace
RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon \
    && find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre
WORKDIR /app
VOLUME /tmp

COPY --from=builder /workspace/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
