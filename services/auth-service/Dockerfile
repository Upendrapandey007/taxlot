FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY gradlew* gradle* ./
COPY gradle gradle
RUN chmod +x gradlew
COPY settings.gradle.kts build.gradle.kts ./
COPY shared shared
COPY services/auth-service services/auth-service
RUN ./gradlew :services:auth-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S taxlot && adduser -S taxlot -G taxlot
WORKDIR /app
COPY --from=builder /workspace/services/auth-service/build/libs/*.jar app.jar
RUN chown taxlot:taxlot app.jar
USER taxlot
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
