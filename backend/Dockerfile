# ═══════════════════════════════════════════════════════════════
# CareNest Backend - Multi-stage Dockerfile
# ═══════════════════════════════════════════════════════════════
# Stage 1 (builder) : Maven + JDK 17  → build .jar
# Stage 2 (runtime) : eclipse-temurin JRE 17 Alpine → run .jar
# Final image size  : ~200MB (vs ~600MB with full JDK)
# ═══════════════════════════════════════════════════════════════

# ──────────────────────────────────────────────────────────────
# STAGE 1: BUILDER
# ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml first for dependency caching layer
COPY pom.xml .

# Download all dependencies (separate layer = faster rebuilds when only src changes)
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy the full source code
COPY src ./src

# Build the application, skipping tests (tests run in CI/CD pipeline separately)
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ──────────────────────────────────────────────────────────────
# STAGE 2: RUNTIME (Production-ready, minimal image)
# ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -S carenest && adduser -S carenest -G carenest

# Copy ONLY the compiled .jar from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to the non-root user
RUN chown carenest:carenest app.jar

# Switch to non-root user
USER carenest

# Expose API port
EXPOSE 8080

# Health check for Docker / orchestrators (Kubernetes, Compose)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/actuator/health || exit 1

# JVM tuning for containers:
# -XX:+UseContainerSupport  → respect Docker memory limits
# -XX:MaxRAMPercentage=75.0 → use 75% of allocated container memory
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
