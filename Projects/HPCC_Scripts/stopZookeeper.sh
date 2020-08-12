#!/usr/bin/env bash
ZOOKEEPER_CONFIG_NAME="zoo.cfg"

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

ZOOKEEPER_CONF_PATH=${TMPDIR}/zookeeper
ZOOKEEPER_DATADIR_PATH=${TMPDIR}/zookeeper

# shellcheck disable=SC2154
for nodeToSetup in ${expandedNodeList}; do
	zookeepercfg="${ZOOKEEPER_CONF_PATH}/${ZOOKEEPER_CONFIG_NAME}"
	# Stop zookeeper!
	echo "Stopping zookeeper on ${nodeToSetup}"
	cd "${zookeeperdatadirpath}" || exit 1
	# We need to SSH in and run the command so that it stays running
	# shellcheck disable=SC2029
	ssh "${nodeToSetup}" "${ZOOKEEPER_PATH}/zkServer.sh stop ${zookeepercfg}"
	echo "Status for ${nodeToSetup}: ${?}"
done



