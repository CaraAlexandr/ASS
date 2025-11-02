FROM eclipse-temurin:21-jdk AS build

# Build argument to force cache invalidation
ARG CACHE_BUST=1

WORKDIR /app

# Copy build timestamp file first (changes on every build attempt)
COPY pom.xml .
COPY src ./src

RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:21-jre

WORKDIR /app

# Install basic utilities for HTTP requests (API approach)
RUN apt-get update && \
    apt-get install -y curl wget && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

