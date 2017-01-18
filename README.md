kixi.eventlog
=============

A kixi module to accept events and store them


deployment prerequesites
------------------------

- You will need an account on [Docker Hub](http://hub.docker.com) and be a member of the MastodonC organization.

- You will need to be logged in to that account from docker's point of view (You only need to do this once).

```
docker login
```

- Choose whether the docker image should use authentication. If not, use:
```
docker build -t mastodonc/kixi.eventlog --build-arg AUTHENTICATION=false .
```

- If authentication is required, then the secrets need to be downloaded from an S3 bucket
```
docker build -t mastodonc/kixi.eventlog.auth --build-arg SECRETS_BUCKET=your-bucket --build-arg AUTHENTICATION=true --build-arg AWS_REGION=eu-central-1 .
```



deployment
----------

To build the ``kixi.eventlog`` docker image and publish it to [Docker Hub](http://hub.docker.com)

```
# this throws exceptions about LOGSTASH port is undefined that can be ignored
./build-and-push-to-hub
```
