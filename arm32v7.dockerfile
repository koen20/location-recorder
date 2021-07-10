FROM arm32v7/adoptopenjdk@sha256:5e402bdceb6ff79a07c131137d646d2924ff9d116a40902172e89cf7c41d192c

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
