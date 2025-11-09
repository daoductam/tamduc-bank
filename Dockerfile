#stage 1 : build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

#set the working directory inside the container
WORKDIR /app

#Install the apache maven build toool
#update package lissts ans install Maven without recommends packages to keep the layer small
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

#stage 2 is to build aproduction ready image

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

#copy the final executable JAR file from the builder stage;s target directory
#this is a key advantage of multi-stage buildds: only the artifact is copied, not build tools or source
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]