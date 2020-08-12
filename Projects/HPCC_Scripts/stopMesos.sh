#!/usr/bin/env bash
set -x
# Launch this with one instance


# shellcheck disable=SC1091
source ./setEnvironmentVars.bash
 
# Setup useful variables
# shellcheck disable=SC2034
scriptName=$(basename "$0")

echo "master: ${masterNode}"
echo "agents: ${slaveNodes}"


rm -rf $MESOS_MASTER_IP
rm -rf $MESOS_MASTER_PORT

for nodeToSetup in ${masterNode}; do
	nodeToSetup="$nodeToSetup-e0"

	echo "Stopping mesos-master on ${nodeToSetup}"
	ssh "${nodeToSetup}" "killall mesos-master"
	echo "Status for ${nodeToSetup}: ${?}"

	ssh "${nodeToSetup}" "killall mesos-agent"
	echo "Status for ${nodeToSetup}: ${?}"
	sleep 1
done

for nodeToSetup in ${slaveNodes}; do
	nodeToSetup="$nodeToSetup-e0"

	echo "Stopping mesos-agent on ${nodeToSetup}"
	ssh "${nodeToSetup}" "killall mesos-agent"
	echo "Status for ${nodeToSetup}: ${?}"
	sleep 1
done


