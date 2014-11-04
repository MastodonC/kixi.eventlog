#!/bin/bash

ZK01=$(grep '\bzk01\b' /etc/hosts)
ZK02=$(grep '\bzk02\b' /etc/hosts)
ZK03=$(grep '\bzk03\b' /etc/hosts)

if [ -z "${ZK01}" ]; then
    if [ -z "${ZK01_PORT_2181_TCP_ADDR}" ]; then
	echo "No zk01 configured, exiting..."
	exit 1;
    fi
    hosts_entry="${ZK01_PORT_2181_TCP_ADDR} zk01"
    echo "Adding zk01 entry:"
    echo -e "\t${hosts_entry}"
    echo "${hosts_entry}" >> /etc/hosts
else
    echo "Retaining existing zk01 entry:"
    echo -e "\t ${ZK01}"
fi

if [ -z "${ZK02}" ]; then
    echo "${ZK02_PORT_2181_TCP_ADDR} zk02" >> /etc/hosts
else
    echo "Retaining existing zk02 entry:"
    echo -e "\t ${ZK02}"
fi

if [ -z "${ZK03}" ]; then
    echo "${ZK03_PORT_2181_TCP_ADDR} zk03" >> /etc/hosts
else
    echo "Retaining existing zk03 entry:"
    echo -e "\t ${ZK03}"
fi

echo "Starting uberjar..."
java -jar /uberjar.jar
