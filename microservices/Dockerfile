# One Dockerfile for every service:  docker build --build-arg SERVICE=product-service -t achat/product-service .
# The build context is the microservices/ folder so the parent POM is available.

FROM maven:3.8.8-eclipse-temurin-8 AS build
ARG SERVICE
WORKDIR /build

# Copy the poms first so dependency download is cached until a pom changes.
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY product-service/pom.xml product-service/
COPY supplier-service/pom.xml supplier-service/
COPY invoice-service/pom.xml invoice-service/
RUN mvn -B -q -pl ${SERVICE} -am dependency:go-offline

COPY . .
RUN mvn -B -q -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:8-jre-alpine
ARG SERVICE
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/${SERVICE}/target/app.jar app.jar
USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
