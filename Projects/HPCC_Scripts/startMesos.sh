#!/usr/bin/env bash
set -x
# Launch this with one instance


startMesos="yes"
runAgentOnMasterNode="no"

./setEnvironmentVars.sh


# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash
 
MESOS_LOCAL_PATH=${TMPDIR}/mesos

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

masterPort=5050
echo "master: ${masterNode}"
echo "agents: ${slaveNodes}"

if [[ "${startMesos}" == 'yes' ]]; then

	rm -rf $MESOS_MASTER_IP
	rm -rf $MESOS_MASTER_PORT

	for nodeToSetup in ${masterNode}; do
		echo "Setting up $nodeToSetup as master"
		nodeToSetup="$nodeToSetup-e0"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')
		masterIp=${tempIp}

		# Create working directory
		ssh "${nodeToSetup}" "mkdir -p ${MESOS_LOCAL_PATH}/${nodeToSetup}"

		echo "Starting mesos-master on ${nodeToSetup}"
		ssh "${nodeToSetup}" "${TMP_PREFIX}/sbin/mesos-daemon.sh mesos-master --ip=${tempIp} --work_dir=${MESOS_LOCAL_PATH}/${nodeToSetup} --no-hostname_lookup </dev/null >/dev/null"
		echo "Status for ${nodeToSetup}: ${?}"
		sleep 1

		if [[ "${runAgentOnMasterNode}" == 'yes' ]]; then
			echo "Also launching mesos-agent on master"
			ssh "${nodeToSetup}" "${TMP_PREFIX}/sbin/mesos-daemon.sh mesos-agent --master=${masterIp}:${masterPort} --work_dir=${MESOS_LOCAL_PATH}/${nodeToSetup} --no-systemd_enable_support --no-switch_user --no-hostname_lookup </dev/null >/dev/null"

		fi
	done

	for nodeToSetup in ${slaveNodes}; do
		echo "Setting up $nodeToSetup as agent"
		nodeToSetup="$nodeToSetup-e0"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')

		# Create working directory
		ssh "${nodeToSetup}" "mkdir -p ${MESOS_LOCAL_PATH}/${nodeToSetup}"

		echo "Starting mesos-agent on ${nodeToSetup}"
		ssh "${nodeToSetup}" "${TMP_PREFIX}/sbin/mesos-daemon.sh mesos-agent --master=${masterIp}:${masterPort} --work_dir=${MESOS_LOCAL_PATH}/${nodeToSetup} --no-systemd_enable_support --no-switch_user --no-hostname_lookup </dev/null >/dev/null"
		echo "Status for ${nodeToSetup}: ${?}"
		sleep 1
	done


	echo -n "${masterIp}" >> "${MESOS_MASTER_IP}"
	echo -n "${masterPort}" >> "${MESOS_MASTER_PORT}"

fi

