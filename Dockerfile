FROM gcr.io/distroless/java17-debian11:debug
COPY  . /app
WORKDIR /app
EXPOSE 8080
CMD ["target/craftbeer-0.0.1-SNAPSHOT.jar"]