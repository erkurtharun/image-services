# ---------- Build stage ----------
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY image-processing-api/pom.xml image-processing-api/pom.xml
COPY image-upload-api/pom.xml image-upload-api/pom.xml

RUN mvn -q -DskipTests -pl image-processing-api -am dependency:go-offline

COPY shared/src shared/src
COPY image-processing-api/src image-processing-api/src

RUN mvn -q -DskipTests -pl image-processing-api -am package
# Verify the JAR is created correctly
RUN ls -l /workspace/image-processing-api/target/
# Debug: Display MANIFEST.MF contents
RUN unzip -p /workspace/image-processing-api/target/image-processing-api-0.1.0-SNAPSHOT.jar META-INF/MANIFEST.MF || true

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN useradd -ms /bin/bash appuser
USER appuser

WORKDIR /app

COPY --from=build /workspace/image-processing-api/target/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75 -Dsun.net.inetaddr.ttl=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
