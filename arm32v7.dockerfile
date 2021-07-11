FROM arm32v7/adoptopenjdk:11-jdk as builder

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN chmod +x gradlew
RUN ./gradlew clean build
RUN tar -xvf server/build/distributions/server-1.0.tar

FROM arm32v7/adoptopenjdk:11-jre

ENV APPLICATION_USER ktor
RUN useradd -ms /bin/bash $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY --from=builder /usr/src/app/server-1.0/ /app/
WORKDIR /config

CMD /app/bin/server
