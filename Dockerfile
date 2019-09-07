FROM openjdk:8-jre-alpine

RUN mkdir /recorder
WORKDIR /recorder
RUN wget https://github.com/koen20/Owntracks-mysql-recorder/releases/download/V1.0/owntracks-mysql-1.0.zip
RUN unzip owntracks-mysql-1.0.zip
CMD /recorder/owntracks-mysql-1.0/bin/owntracks-mysql
