#!/usr/bin/env bash
set -x
# Launch this with one instance


startKafka="yes"

./setEnvironmentVars.sh

TEMPLATE_CONFIG_FILE_PATH="/home/rcf-40/geoffret/hpcc_scripts/configs/"
TEMPLATE_CONFIG_FILE_PATH="${TMP_PREFIX}/hpcc_scripts/configs/"

KAFKA_CONFIG_NAME="server.properties"

# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

KAFKA_CONF_PATH=${TMPDIR}/kafka
KAFKA_DATADIR_PATH=${TMPDIR}/kafka

KAFKA_COPY_DIR="$(dirname "${KAFKA_PATH}")"

KAFKA_LOCAL_PATH=${TMPDIR}/$(basename "$(dirname ${KAFKA_PATH})")

# Setup useful variables
# shellcheck disable=SC2034
scriptName=$(basename "$0")

# Check if I am a slave node or not
# shellcheck disable=SC2154
if [[ amSlaveNode -ne 0 ]]; then
	echo "I'm a slave node"
	echo "Quitting since master does the setup"
	exit 0
else
	echo "I'm not a slave node"
fi

kafkaNodeList="${expandedNodeList}"
kafkaNodeList=$(echo $kafkaNodeList | awk '{print $1}')

if [[ "${startKafka}" == 'yes' ]]; then
	echo "Starting Kafka"

	rm -rf "${KAFKA_ACTIVE_SERVER_LIST}"

	servercount=1

	# shellcheck disable=SC2154
	for nodeToSetup in ${kafkaNodeList}; do
		nodeToSetup="$nodeToSetup-e0"

		kafkadatadirpath="${KAFKA_DATADIR_PATH}"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')

		# Setup directories
		ssh "${nodeToSetup}" "mkdir -p ${KAFKA_CONF_PATH}"
		ssh "${nodeToSetup}" "mkdir -p ${kafkadatadirpath}"

		# Setup configuration 
		kafkacfg="${KAFKA_CONF_PATH}/${KAFKA_CONFIG_NAME}"
		cp "${TEMPLATE_CONFIG_FILE_PATH}/${KAFKA_CONFIG_NAME}" "${nodeToSetup}.properties"

		# Set variables
		KAFKABROKERID="${servercount}"
		KAFKALISTENERS="${tempIp}:${KAFKA_CLIENT_PORT}"
		KAFKALOGDIR="${kafkadatadirpath}"
		ZOOKEEPERCONNECTIONS=$(cat "${ZOOKEEPER_ACTIVE_SERVER_LIST}")

		# Substitute in variables
		sed -i \
			-e "s/KAFKABROKERID/${KAFKABROKERID}/g" \
			-e "s/KAFKALISTENERS/${KAFKALISTENERS}/g" \
			-e "s@KAFKALOGDIR@${KAFKALOGDIR}@g" \
			-e "s/ZOOKEEPERCONNECTIONS/${ZOOKEEPERCONNECTIONS}/g" \
			"${nodeToSetup}.properties"

		# Copy config to remote node
		rsync -apv "${nodeToSetup}.properties" "${nodeToSetup}:${kafkacfg}"
		# Get rid of the temporary config
		rm -rf "${nodeToSetup}.properties"

		# Copy kafka to remote node
		rsync -apv "${KAFKA_COPY_DIR}" "${nodeToSetup}:${TMPDIR}/"

		# Start kafka!
		echo "Starting kafka on ${nodeToSetup}"
		# shellcheck disable=SC2029
		ssh "${nodeToSetup}" "cd ${kafkadatadirpath} && ${KAFKA_LOCAL_PATH}/bin/kafka-server-start.sh -daemon ${kafkacfg}"
		sleep 1
		echo "Status for ${nodeToSetup}: ${?}"
		# tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')
		tempIp="${nodeToSetup}"

		# Save to active list
		if [ -f "${KAFKA_ACTIVE_SERVER_LIST}" ]; then
			echo -n "," >> "${KAFKA_ACTIVE_SERVER_LIST}"
		fi
		echo -n "${KAFKALISTENERS}" >> "${KAFKA_ACTIVE_SERVER_LIST}"

		servercount=$((++servercount))
	done

# $2/bin/kafka-server-start.sh -daemon $1/server.properties
# $2/bin/kafka-server-stop.sh  

fi




