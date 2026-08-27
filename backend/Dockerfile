# --- Build stage ---
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon clean installDist

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/build/install/home-jobs-tracker-backend ./
RUN mkdir -p /data/attachments && chown -R appuser:appuser /app /data
USER appuser

ENV HTTP_PORT=8080
EXPOSE 8080

ENTRYPOINT ["./bin/home-jobs-tracker-backend"]
