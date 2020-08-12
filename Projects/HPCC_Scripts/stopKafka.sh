#!/usr/bin/env bash
KAFKA_CONFIG_NAME="server.properties"

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

KAFKA_CONF_PATH=${TMPDIR}/kafka
KAFKA_DATADIR_PATH=${TMPDIR}/kafka

# shellcheck disable=SC2154
for nodeToSetup in ${expandedNodeList}; do
	kafkadatadirpath="${KAFKA_DATADIR_PATH}"
	kafkacfg="${KAFKA_CONF_PATH}/${nodeToSetup}/${KAFKA_CONFIG_NAME}"
	# Stop kafka!
	echo "Stopping kafka on ${nodeToSetup}"
	cd "${kafkadatadirpath}" || exit 1
	# We need to SSH in and run the command so that it stays running
	# shellcheck disable=SC2029
	ssh "${nodeToSetup}" "cd ${kafkadatadirpath} && ${KAFKA_PATH}/kafka-server-stop.sh ${kafkacfg}"
	echo "Status for ${nodeToSetup}: ${?}"
done
