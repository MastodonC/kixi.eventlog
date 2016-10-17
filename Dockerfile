FROM mastodonc/basejava

ARG SECRETS_BUCKET
ENV SECRETS_BUCKET=$SECRETS_BUCKET

ARG AUTHENTICATION
ENV AUTHENTICATION=$AUTHENTICATION

EXPOSE 4001

ADD target/kixi.heimdall.jar /root/kixi.eventlog.jar
ADD docker/start-eventlog.sh start-eventlog

CMD ["/bin/bash", "/start-eventlog"]
