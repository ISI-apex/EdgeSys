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



# Stop observers
for nodeToSetup in ${slaveNodes}; do
	nodeToSetup="$nodeToSetup-e0"

	echo "Stopping thermos_observer on ${nodeToSetup}"
	ssh "${nodeToSetup}" "pkill -f thermos_observer"
	echo "Status for ${nodeToSetup}: ${?}"
	sleep 1
done

# Stop scheduler and observer on master node
for nodeToSetup in ${masterNode}; do
	nodeToSetup="$nodeToSetup-e0"

	ssh "${nodeToSetup}" "pkill -f thermos_observer"
	echo "Status for ${nodeToSetup}: ${?}"

	echo "Stopping aurora-scheduler on ${nodeToSetup}"
	ssh "${nodeToSetup}" "pkill -f \"java -server\""
	echo "Status for ${nodeToSetup}: ${?}"
	sleep 1
done


rm -rf $AURORA_MASTER_IP
rm -rf $AURORA_MASTER_PORT

