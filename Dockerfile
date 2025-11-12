# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Leverage Docker layer cache
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q -e -B -DskipTests clean package

# ===== Run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN useradd -ms /bin/bash appuser

# Copy fat jar
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=UTC

EXPOSE 8080
USER appuser

# Healthcheck (optional, Dokploy may use its own)
# HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
#   CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["/bin/sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
