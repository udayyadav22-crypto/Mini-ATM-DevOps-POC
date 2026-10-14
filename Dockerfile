FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY java-backend/MiniATMServer.java .
COPY java-backend/sqlite-jdbc.jar .
RUN javac -cp sqlite-jdbc.jar MiniATMServer.java

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/MiniATMServer.class .
COPY --from=build /src/sqlite-jdbc.jar .
COPY browser-frontend/ ./browser-frontend/
ENV ATM_DB_PATH=/data/atm.db
ENV ATM_HTML_PATH=/app/browser-frontend/index.html
RUN mkdir -p /data
EXPOSE 8080
ENTRYPOINT ["java", "-cp", ".:sqlite-jdbc.jar", "MiniATMServer"]
