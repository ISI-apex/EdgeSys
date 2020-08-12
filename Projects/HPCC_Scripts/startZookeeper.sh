#!/usr/bin/env bash
set -x
# Launch this with one instance


startZookeeper="yes"

./setEnvironmentVars.sh

ZOOKEEPER_CONFIG_NAME="zoo.cfg"
TEMPLATE_CONFIG_FILE_PATH="/home/rcf-proj2/jnw/geoffret/hpcc_scripts/configs/"
TEMPLATE_CONFIG_FILE_PATH="${TMP_PREFIX}/hpcc_scripts/configs/"


# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

ZOOKEEPER_CONF_PATH=${TMPDIR}/zookeeper
ZOOKEEPER_DATADIR_PATH=${TMPDIR}/zookeeper

ZOOKEEPER_COPY_DIR="$(dirname "${ZOOKEEPER_PATH}")"

ZOOKEEPER_LOCAL_PATH=${TMPDIR}/$(basename "$(dirname ${ZOOKEEPER_PATH})")

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

zookeeperNodeList="${expandedNodeList}"
# Launch only on first node in list (smallest ID)
zookeeperNodeList=$(echo $zookeeperNodeList | awk '{print $1}')

if [[ "${startZookeeper}" == 'yes' ]]; then
	rm -rf "${ZOOKEEPER_ACTIVE_SERVER_LIST}"

	# shellcheck disable=SC2154
	for nodeToSetup in ${zookeeperNodeList}; do
		nodeToSetup="$nodeToSetup-e0"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')

		# Create directory for configuration
		# shellcheck disable=SC2154
		ssh "${nodeToSetup}" "mkdir -p ${ZOOKEEPER_CONF_PATH}"

		# Generate configuration files
		zookeepercfg="${ZOOKEEPER_CONF_PATH}/${ZOOKEEPER_CONFIG_NAME}"
		# Copy template file
		cp "${TEMPLATE_CONFIG_FILE_PATH}/${ZOOKEEPER_CONFIG_NAME}" "${nodeToSetup}.cfg"

		# Setup parameters
		zookeeperdatadirpath="${ZOOKEEPER_DATADIR_PATH}"
		zookeeperticktime=2000
		zookeeperinitlimit=50
		zookeepersynclimit=2
		default_zookeeper_client_port=4121
		ZOOKEEPER_THIS_HOST=${tempIp}
		# Create data directory
		ssh "${nodeToSetup}" "mkdir -p ${zookeeperdatadirpath}"
		# Substitute in variables
		sed -i \
			-e "s@ZOOKEEPERDATADIR@${zookeeperdatadirpath}@g" \
			-e "s/ZOOKEEPERTICKTIME/${zookeeperticktime}/g" \
			-e "s/ZOOKEEPERINITLIMIT/${zookeeperinitlimit}/g" \
			-e "s/ZOOKEEPERSYNCLIMIT/${zookeepersynclimit}/g" \
			-e "s/ZOOKEEPERCLIENTPORT/${default_zookeeper_client_port}/g" \
			-e "s/ZOOKEEPERHOST/${ZOOKEEPER_THIS_HOST}/g" \
			"${nodeToSetup}.cfg"

		# Setup myid files
		servercount=1
		# shellcheck disable=SC2154
		for zookeepernode in ${zookeeperNodeList}
		do
			zookeepernode="$zookeepernode-e0"
			tempIp=$(getent hosts "${zookeepernode}" | awk '{ print $1 }')
			# tempIp="${zookeepernode}"
			echo "server.${servercount}=${tempIp}:${ZOOKEEPER_PEER_PORT}:${ZOOKEEPER_LEADER_PORT}" >> "${nodeToSetup}.cfg"
			if [[ "$zookeepernode" == "${nodeToSetup}" ]]; then
				echo "${servercount}" > "${nodeToSetup}".myid
				echo
			fi
			servercount=$((++servercount))
		done

		# Copy config to remote node
		rsync -apv "${nodeToSetup}.cfg" "${nodeToSetup}:${zookeepercfg}"
		# Copy myid to remote node
		rsync -apv "${nodeToSetup}".myid "${nodeToSetup}:${ZOOKEEPER_DATADIR_PATH}/myid"

		# Get rid of the temporary config
		rm -rf "${nodeToSetup}.cfg"
		rm -rf "${nodeToSetup}".myid

		# Copy zookeeper to remote node
		rsync -apv "${ZOOKEEPER_COPY_DIR}" "${nodeToSetup}:${TMPDIR}/"

		# Start zookeeper!
		echo "Starting zookeeper on ${nodeToSetup}"
		# shellcheck disable=SC2029
		ssh "${nodeToSetup}" "cd ${zookeeperdatadirpath} && ${ZOOKEEPER_LOCAL_PATH}/bin/zkServer.sh start ${zookeepercfg}"
		sleep 1
		echo "Status for ${nodeToSetup}: ${?}"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')
		# tempIp="${nodeToSetup}"

		# Save to active list
		# If file exists, just add separator
		if [ -f "${ZOOKEEPER_ACTIVE_SERVER_LIST}" ]; then
			echo -n "," >> "${ZOOKEEPER_ACTIVE_SERVER_LIST}"
		fi
		echo -n "${tempIp}:${default_zookeeper_client_port}" >> "${ZOOKEEPER_ACTIVE_SERVER_LIST}"
	done
	echo "Zookeeper Nodes: ${zookeeperNodeList}"
fi

