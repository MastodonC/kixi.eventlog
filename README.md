kixi.eventlog
=============

A kixi module to accept events and store them


dev setup
---------

You need to arrange to have docker listening on a tcp port... It should start with a command like:

    /usr/bin/docker -d -H fd:// -H tcp://localhost:2375
