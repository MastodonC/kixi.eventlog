#!/bin/bash

function add_hosts_entry() {
    local var_upper=${1^^}

    if $(grep '\b$1\b' /etc/hosts) ; then
	echo "Retaining existing $1 entry:"
	echo -e "\t ${!var_upper}"
    else
	local host_var="${var_upper}_PORT_2181_TCP_ADDR"
	local hosts_entry="${!host_var} $1"
	echo "Adding $1 entry"
	echo -e "${hosts_entry}"
	echo ${hosts_entry} >> /etc/hosts
    fi
}

if [ -z "${ZK01_PORT_2181_TCP_ADDR}" ]; then
    echo "No zk01 configured, exiting..."
    exit 1;
fi

add_hosts_entry zk01
add_hosts_entry zk02
add_hosts_entry zk03

echo "Starting uberjar..."
java -jar /uberjar.jar
