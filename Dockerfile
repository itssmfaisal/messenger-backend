FROM gradle:8.3-jdk17 AS build
WORKDIR /home/gradle/project
# Copy project files and make `gradle` the owner to avoid permission issues writing cache
COPY --chown=gradle:gradle . .

# Ensure the wrapper is executable and use it so the project Gradle version is respected.
USER root
RUN chmod +x ./gradlew || true && chown -R gradle:gradle /home/gradle/project

USER gradle
ENV GRADLE_USER_HOME=/home/gradle/.gradle
RUN ./gradlew bootJar -x test --no-daemon --stacktrace

FROM eclipse-temurin:17-jdk
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=build/libs/*.jar
COPY --from=build /home/gradle/project/${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
