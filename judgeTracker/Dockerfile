FROM eclipse-temurin:17-jdk-alpine
LABEL authors="RayS"
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ADD system.config /root
WORKDIR /root
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]