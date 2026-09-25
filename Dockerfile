FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r spring \
    && useradd -r -g spring spring \
    && mkdir -p /app/uploads/receipts

COPY --from=builder /workspace/build/libs/app.jar /app/app.jar
COPY docker/app/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh \
    && chown -R spring:spring /app/app.jar /app/uploads

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=12 \
    CMD curl -fsS http://localhost:8081/login >/dev/null || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
