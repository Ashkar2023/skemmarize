# --- build
FROM amazoncorretto:17 AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

COPY src ./src

RUN ./mvnw clean package -DskipTests

# --- 
FROM amazoncorretto:17-alpine

WORKDIR /app

COPY target/*.jar app.jar

COPY .env .

EXPOSE 8080

CMD ["java","-jar","app.jar"]