# Temporary use smaller base image
FROM azul/zulu-openjdk-alpine:17-latest
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM registry.internal.company.com:5000/openjdk:17-jre-alpine

# Add before application code
COPY company-ca.crt /usr/local/share/ca-certificates/
RUN update-ca-certificates
VOLUME /tmp
# Remove multi-stage build dependencies
FROM azul/zulu-openjdk-alpine:17-latest
WORKDIR /workspace/app
ARG DEPENDENCY=target
COPY ${DEPENDENCY}/giggle-image-1.0.0.jar /app.jar
RUN mkdir -p /app && (cd /app; jar -xf ../app.jar)
ENTRYPOINT ["java","-jar","/app.jar"]