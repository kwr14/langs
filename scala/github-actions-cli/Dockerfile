# Multi-stage build for GitHub Actions CLI

# Stage 1: Build with sbt
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.1_12_1.10.0_3.4.2 AS builder

WORKDIR /build

# Copy build files
COPY build.sbt .
COPY project project/

# Copy source code
COPY core core/
COPY api-client api-client/
COPY terminal-ui terminal-ui/
COPY cli cli/

# Build fat JAR
RUN sbt "cli/assembly"

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-jammy

LABEL org.opencontainers.image.title="GitHub Actions CLI"
LABEL org.opencontainers.image.description="Terminal-based GitHub Actions workflow monitoring and management tool"
LABEL org.opencontainers.image.source="https://github.com/kwr14/langs"
LABEL org.opencontainers.image.licenses="MIT"

# Create non-root user
RUN useradd -m -u 1000 -s /bin/bash ghactions

# Copy JAR from builder
COPY --from=builder /build/cli/target/scala-3.5.0/github-actions-cli.jar /app/github-actions-cli.jar

# Set ownership
RUN chown -R ghactions:ghactions /app

# Switch to non-root user
USER ghactions
WORKDIR /app

# Set entrypoint
ENTRYPOINT ["java", "-jar", "/app/github-actions-cli.jar"]

# Default command (show help)
CMD ["--help"]

