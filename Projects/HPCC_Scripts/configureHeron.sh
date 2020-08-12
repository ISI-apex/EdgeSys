#!/usr/bin/env bash
set -x
# Launch this with one instance

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash


HERON_STATE_TEMPLATE=/home/rcf-40/geoffret/stagingDynamo/heron/conf/exampleHPCC/statemgr-template.yaml
HERON_STATE_CONFIG=/home/rcf-40/geoffret/stagingDynamo/heron/conf/exampleHPCC/statemgr.yaml
# HERON_TRACKER_TEMPLATE=/home/rcf-40/geoffret/stagingDynamo/herontools/conf/heron_tracker-template.yaml 
# HERON_TRACKER_CONFIG=/home/rcf-40/geoffret/stagingDynamo/herontools/conf/heron_tracker.yaml 

HERON_TRACKER_TEMPLATE=/home/rcf-40/geoffret/stagingDynamo/heron/conf/heron_tracker-template.yaml 
HERON_TRACKER_CONFIG=/home/rcf-40/geoffret/stagingDynamo/heron/conf/heron_tracker.yaml 

# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

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

# Create paths in zookeeper for Heron
echo "Creating paths for Heron in Zookeeper"
${ZOOKEEPER_PATH}/zkCli.sh -server $(cat $ZOOKEEPER_ACTIVE_SERVER_LIST) create /heronroot null
echo "Create /heronroot status: "$?
${ZOOKEEPER_PATH}/zkCli.sh -server $(cat $ZOOKEEPER_ACTIVE_SERVER_LIST) create /heronroot/topologies null
echo "Create /heronroot/topologies status: "$?
sleep 10

# Used to setup heron to use zookeeper as state location
echo "Setting Heron up to use Zookeeper"
cp "${HERON_STATE_TEMPLATE}" "${HERON_STATE_CONFIG}"
sed -i \
	-e "s/ZOOKEEPERSTRING/$(cat "${ZOOKEEPER_ACTIVE_SERVER_LIST}")/g" \
	"${HERON_STATE_CONFIG}"
sleep 10

# Setup heron tracker configuration
echo "Setting Heron-tracker up to use Zookeeper"
cp "${HERON_TRACKER_TEMPLATE}" "${HERON_TRACKER_CONFIG}"
sed -i \
	-e "s/ZOOKEEPERSTRING/$(cat "${ZOOKEEPER_ACTIVE_SERVER_LIST}")/g" \
	"${HERON_TRACKER_CONFIG}"
sleep 10

echo "Heron is configured!"
