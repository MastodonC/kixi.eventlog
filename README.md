kixi.eventlog
=============

A kixi module to accept events and store them


dev setup
---------

To build the docker images(s) a few prep steps are required:
 - You need to arrange to have docker listening on a tcp port... It should start with a command like:

    /usr/bin/docker -d -H fd:// -H tcp://localhost:2375

 - Then run lein uberimage. This currently creates TWO images, appropriately tagged, but doesn't push them to the public docker repo.


deployment prerequesites
------------------------

You will need an account on [Docker Hub](http://hub.docker.com) and be a member of the MastodonC organization.

You will need to be logged in to that account from docker's point of view (You only need to do this once).

```
docker login
```

deployment
----------

To build the ``kixi.eventlog`` docker image and publish it to [Docker Hub](http://hub.docker.com)

```
lein clean

# this throws exceptions about LOGSTASH port is undefined that can be ignored
lein uberimage

docker push mastodonc/kixi.eventlog:latest
