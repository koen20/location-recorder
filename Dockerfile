FROM eclipse-temurin:11-jdk as builder

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN chmod +x gradlew
RUN ./gradlew clean build
RUN tar -xvf server/build/distributions/server-1.0.tar

FROM eclipse-temurin:11-jre-alpine

ENV APPLICATION_USER ktor
RUN adduser -D $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY --from=builder /usr/src/app/server-1.0/ /app/
WORKDIR /config

CMD /app/bin/server
