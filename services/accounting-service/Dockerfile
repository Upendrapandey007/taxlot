FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY gradlew* gradle* ./
COPY gradle gradle
RUN chmod +x gradlew
COPY settings.gradle.kts build.gradle.kts ./
COPY shared shared
COPY services/accounting-service services/accounting-service
RUN ./gradlew :services:accounting-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S taxlot && adduser -S taxlot -G taxlot
WORKDIR /app
COPY --from=builder /workspace/services/accounting-service/build/libs/*.jar app.jar
RUN chown taxlot:taxlot app.jar
USER taxlot
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8084/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
