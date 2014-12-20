#!/bin/bash

function join { local IFS="$1"; shift; echo "$*"; }

#Add entries for zookeeper peers.
hosts=()
for i in $(seq 255)
do
    zk_name=$(printf "ZK%02d" ${i})
    zk_addr_name="${zk_name}_PORT_2181_TCP_ADDR"
    zk_port_name="${zk_name}_PORT_2181_TCP_PORT"

    [ ! -z "${!zk_addr_name}" ] && hosts+=("${!zk_addr_name}:${!zk_port_name}/kafka")
done

export ZK_CONNECT=$(join , ${hosts[@]})
echo "Zookeeper connect string is ${ZK_CONNECT}"

echo "Starting uberjar..."
java -jar /uberjar.jar
