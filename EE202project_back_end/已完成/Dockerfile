###############################
# 1. Resolve Maven Dependencies
###############################
FROM eclipse-temurin:21-jdk-jammy AS deps

WORKDIR /build

COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

RUN ./mvnw dependency:go-offline -DskipTests

###############################
# 2. Package Spring Boot
###############################
FROM deps AS package

WORKDIR /build
COPY src src

RUN ./mvnw -DskipTests package \
    && mv target/*.jar app.jar

###############################
# 3. Extract Spring Boot Layers
###############################
FROM package AS extract

WORKDIR /build
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted

###############################
# 4. Final Runtime Image
###############################
FROM eclipse-temurin:21-jre-jammy AS final

WORKDIR /app

# Create non-root user
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/usr/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser && \
    mkdir -p /app/uploads && chown -R appuser /app/uploads

USER appuser

# Copy Spring Boot Layers from extract stage
COPY --from=extract /build/extracted/dependencies/ ./
COPY --from=extract /build/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/extracted/spring-boot-loader/ ./
COPY --from=extract /build/extracted/application/ ./

EXPOSE 8082

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
