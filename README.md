kixi.eventlog
=============

A kixi module to accept events and store them


dev setup
---------

To build the docker images(s) a few prep steps are required:
 - You need to arrange to have docker listening on a tcp port... It should start with a command like:

    /usr/bin/docker -d -H fd:// -H tcp://localhost:2375

 - Then run lein uberimage. This currently creates TWO images, appropriately tagged, but doesn't push them to the public docker repo.
