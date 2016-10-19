FROM phusion/baseimage:0.9.19

ARG SECRETS_BUCKET
ENV SECRETS_BUCKET=$SECRETS_BUCKET

ARG AUTHENTICATION
ENV AUTHENTICATION=$AUTHENTICATION

CMD ["/sbin/my_init"]

RUN apt-get install software-properties-common
RUN add-apt-repository -y ppa:webupd8team/java \
&& apt-get update \
&& echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
&& apt-get install -y \
software-properties-common \
oracle-java8-installer


RUN apt-get install -y python2.7 \
unzip

RUN ln -s /usr/bin/python2.7 /usr/bin/python

EXPOSE 4001

RUN mkdir /etc/service/eventlog

ADD target/kixi.eventlog.jar /srv/kixi.eventlog.jar
ADD docker/start-eventlog.sh /etc/service/eventlog/run

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*