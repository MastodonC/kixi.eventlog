#!/bin/bash

function join { local IFS="$1"; shift; echo "$*"; }

if [ -z "${ZK_CONNECT}" ] ; then

    ZK_CHROOT=${ZK_CHROOT:-/}
    echo "ZK_CHROOT is ${ZK_CHROOT}"

    #Add entries for zookeeper peers.
    hosts=()
    for i in $(seq 255)
    do
        zk_name=$(printf "ZK%02d" ${i})
        zk_addr_name="${zk_name}_PORT_2181_TCP_ADDR"
        zk_port_name="${zk_name}_PORT_2181_TCP_PORT"

        [ ! -z "${!zk_addr_name}" ] && hosts+=("${!zk_addr_name}:${!zk_port_name}")
    done

    export ZK_CONNECT="$(join , ${hosts[@]})${ZK_CHROOT}"

fi

if [ -z "${TOPICS}" ]; then
    topics=()
    for t in {1..255}
    do
        topic_name=$(printf "TOPIC%02d" ${t})
        [ ! -z "${!topic_name}" ] && topics+=("\"${!topic_name}\"")
    done

    export TOPICS="#{${topics[@]}}"

fi

echo "Zookeeper connect string is ${ZK_CONNECT}"
echo "TOPICS is ${TOPICS}"

echo "Starting uberjar..."
java -jar /uberjar.jar
