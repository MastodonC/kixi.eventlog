#!/bin/bash

function join { local IFS="$1"; shift; echo "$*"; }

CGROUPS_MEM=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)
MEMINFO_MEM=$(($(awk '/MemTotal/ {print $2}' /proc/meminfo)*1024))
MEM=$(($MEMINFO_MEM>$CGROUPS_MEM?$CGROUPS_MEM:$MEMINFO_MEM))
JVM_PEER_HEAP_RATIO=${JVM_PEER_HEAP_RATIO:-0.6}
XMX=$(awk '{printf("%d",$1*$2/1024^2)}' <<< " ${MEM} ${JVM_PEER_HEAP_RATIO} ")

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

if [[ "${SECRETS_BUCKET}" ]]; then
    curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o "/root/awscli-bundle.zip"
    unzip /root/awscli-bundle.zip -d /root/
    /root/awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws
    echo "aws cli installed"
    aws s3 cp s3://$SECRETS_BUCKET/prod_pubkey.edn /root/prod_pubkey.edn
    echo "public key downloaded"
fi

echo "Starting uberjar..."
echo "java -Xmx${XMX}m -jar /srv/kixi.eventlog.jar --authentication $AUTHENTICATION --profile production"
java "-Xmx${XMX}m" -jar /srv/kixi.eventlog.jar --authentication $AUTHENTICATION --profile production
