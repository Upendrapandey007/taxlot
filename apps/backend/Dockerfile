# =============================================================================
# Taxlot Backend — Multi-stage Dockerfile
# =============================================================================
# Build stage: compile and package the application
# Runtime stage: minimal JRE image, non-root user
# =============================================================================

# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable and download dependencies (cached layer)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the fat JAR
RUN ./gradlew bootJar --no-daemon -x test

# Extract layers for efficient Docker caching
RUN mkdir -p /workspace/target && \
    java -Djarmode=layertools -jar build/libs/*.jar extract --destination target

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: create non-root user
RUN addgroup -S taxlot && adduser -S taxlot -G taxlot

WORKDIR /app

# Copy layered JAR content (order matters — least to most frequently changed)
COPY --from=builder /workspace/target/dependencies/ ./
COPY --from=builder /workspace/target/spring-boot-loader/ ./
COPY --from=builder /workspace/target/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/application/ ./

# Set ownership
RUN chown -R taxlot:taxlot /app

# Switch to non-root user
USER taxlot

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Start application
ENTRYPOINT ["sh", "-c", "java  org.springframework.boot.loader.launch.JarLauncher"]
