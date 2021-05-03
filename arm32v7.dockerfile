FROM alpine AS builder

# Download QEMU, see https://github.com/docker/hub-feedback/issues/1261
#ENV QEMU_URL https://github.com/balena-io/qemu/releases/download/v3.0.0%2Bresin/qemu-3.0.0+resin-arm.tar.gz
ENV QEMU_URL https://github.com/balena-io/qemu/releases/download/v5.2.0%2Bbalena4/qemu-5.2.0.balena4-arm.tar.gz
RUN apk add curl && curl -L ${QEMU_URL} | tar zxvf - -C . --strip-components 1

FROM arm32v7/adoptopenjdk@sha256:5e402bdceb6ff79a07c131137d646d2924ff9d116a40902172e89cf7c41d192c

COPY --from=builder qemu-arm-static /usr/bin

RUN mkdir /usr/src/app
COPY ./build/distributions/owntracks-mysql-1.0.tar /usr/src/app

WORKDIR /usr/src/app
RUN tar -xvf ./owntracks-mysql-1.0.tar && rm owntracks-mysql-1.0.tar

ENV APPLICATION_USER ktor
RUN useradd -ms /bin/bash $APPLICATION_USER

WORKDIR /config
RUN chown -R $APPLICATION_USER /usr/src/app
RUN chown -R $APPLICATION_USER /config
USER $APPLICATION_USER
CMD /usr/src/app/owntracks-mysql-1.0/bin/owntracks-mysql
