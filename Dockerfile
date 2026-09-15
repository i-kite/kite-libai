# ---------- build stage ----------
FROM eclipse-temurin:11-jdk-jammy AS builder

WORKDIR /workspace

# Copy build scripts first so dependency resolution is cached independently of source changes.
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle gradle
RUN ./gradlew --no-daemon -q help

COPY src src

# Tests already ran in the CI test job; skip them here to keep the image build fast.
RUN ./gradlew --no-daemon clean bootJar -x test

# Split the fat jar into layers so Docker can cache dependencies separately from app code.
RUN cp "$(find build/libs -name '*.jar' ! -name '*-plain.jar' | head -n 1)" app.jar \
    && java -Djarmode=layertools -jar app.jar extract --destination layers

# ---------- runtime stage ----------
FROM eclipse-temurin:11-jre-jammy

# Run as a non-root user.
RUN groupadd --system --gid 1001 spring \
    && useradd --system --uid 1001 --gid spring --create-home spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /workspace/layers/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/layers/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/layers/application/ ./

USER spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# `exec` keeps the JVM as PID 1 so SIGTERM reaches it and shutdown stays graceful.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
