#!/usr/bin/env bash



export TMP_PREFIX="/scratch2/geoffret/edgesysHPCC"
export TMP_LIBS_DIR="${TMP_PREFIX}/lib"
export TMP_BUILD_DIR="${TMP_PREFIX}/buildFiles"
export CLUSTER_NAME="testCluster"
export TMP_RUNTIME_CLUSTER_INFO_DIR="$TMP_PREFIX/${CLUSTER_NAME}Info"
export USER_NAME="geoffret"


# ****** CONF/DATADIR not used since we use TMPDIR
# Configuration
ZOOKEEPER_PATH=${TMP_BUILD_DIR}/zookeeper-3.4.10/bin
# ZOOKEEPER_CONF_PATH=/staging/jnw/geoffret/confFiles/zookeeper
# ZOOKEEPER_DATADIR_PATH=/staging/jnw/geoffret/dataDirs/zookeeper
# ZOOKEEPER_CONF_PATH=/tmp/geoffret/confFiles/zookeeper
# ZOOKEEPER_DATADIR_PATH=/tmp/geoffret/dataDirs/zookeeper
ZOOKEEPER_CLIENT_PORT=2181
ZOOKEEPER_PEER_PORT=48912
ZOOKEEPER_LEADER_PORT=48913
# ZOOKEEPER_PEER_PORT=2888
# ZOOKEEPER_LEADER_PORT=3888


KAFKA_PATH=${TMP_BUILD_DIR}/kafka_2.11-0.10.0.1/bin
# KAFKA_CONF_PATH=/staging/jnw/geoffret/confFiles/kafka
# KAFKA_DATADIR_PATH=/staging/jnw/geoffret/dataDirs/kafka
KAFKA_CLIENT_PORT=9092

ZOOKEEPER_ACTIVE_SERVER_LIST=${TMP_RUNTIME_CLUSTER_INFO_DIR}/zookeeperActiveServers
KAFKA_ACTIVE_SERVER_LIST=${TMP_RUNTIME_CLUSTER_INFO_DIR}/kafkaActiveServers
MESOS_MASTER_IP=${TMP_RUNTIME_CLUSTER_INFO_DIR}/mesosMasterIp
MESOS_MASTER_PORT=${TMP_RUNTIME_CLUSTER_INFO_DIR}/mesosMasterPort
AURORA_MASTER_IP=${TMP_RUNTIME_CLUSTER_INFO_DIR}/auroraMasterIp
AURORA_MASTER_PORT=${TMP_RUNTIME_CLUSTER_INFO_DIR}/auroraMasterPort
AURORA_DIST_PATH=${TMP_BUILD_DIR}/aurora-0.14.0
AURORA_CONFIG_ROOT=$AURORA_DIST_PATH/tools/etc/aurora/

HERON_TRACKER_PORT=${TMP_RUNTIME_CLUSTER_INFO_DIR}/heronTrackerPort

SCRIPT_STATUS=${TMP_RUNTIME_CLUSTER_INFO_DIR}/scriptStatus
CLUSTER_SLURM_JOB_ID_FILE=${TMP_RUNTIME_CLUSTER_INFO_DIR}/slurmJobId

# Setup platform
platform='unknown'
unamestr=$(uname)
if [[ "$unamestr" == 'Linux' ]]; then
   platform='linux'
elif [[ "$unamestr" == 'Darwin' ]]; then
   platform='darwin'
fi


# Setup magpie script locations
if [[ "${platform}" == "linux" ]]; then
	EXPAND_NODES=${TMP_BUILD_DIR}/magpie/bin/magpie-expand-nodes
elif [[ "${platform}" == "darwin" ]]; then
	EXPAND_NODES=/Users/Geoffrey/Desktop/magpie/bin/magpie-expand-nodes
fi

export PATH="${TMP_PREFIX}/bin":${PATH}
export PATH="${TMP_BUILD_DIR}/apache-maven-3.5.2/bin":${PATH}
export PATH="/home/rcf-40/geoffret/dynamoProjDir/buildFiles2/dstat-0.7.3":${PATH}
export PATH="${TMP_BUILD_DIR}/aurora-0.14.0/tools/usr/bin":${PATH}


# Setup useful variables
expandedNodeList=$($EXPAND_NODES "${SLURM_JOB_NODELIST}")
myName=$(hostname)


# Setup master/slave node lists
masterNode=$($EXPAND_NODES "${SLURM_JOB_NODELIST}" | head -1)
slaveNodes=$($EXPAND_NODES "${SLURM_JOB_NODELIST}" | tail -n+2)




# Set to 0 if am master, 1 if slave node
amSlaveNode=$(echo "${slaveNodes[@]}" | grep -o "${myName}" | wc -w)


export ZOOKEEPER_PATH
export ZOOKEEPER_CONF_PATH
export ZOOKEEPER_DATADIR_PATH
export ZOOKEEPER_CLIENT_PORT
export ZOOKEEPER_PEER_PORT
export ZOOKEEPER_LEADER_PORT
export KAFKA_PATH
export KAFKA_CONF_PATH
export KAFKA_DATADIR_PATH
export KAFKA_CLIENT_PORT
export platform
export myName
export expandedNodeList
export masterNode
export slaveNodes
export amSlaveNode

export ZOOKEEPER_ACTIVE_SERVER_LIST
export KAFKA_ACTIVE_SERVER_LIST
export MESOS_MASTER_IP
export MESOS_MASTER_PORT
export AURORA_MASTER_IP
export AURORA_MASTER_PORT
export AURORA_CONFIG_ROOT




# shellcheck disable=SC1091
source /usr/usc/java/1.8.0_45/setup.sh
source /usr/usc/python/2.7.8/setup.sh
source /usr/usc/gnu/autoconf/2.69/setup.sh
source /usr/usc/perl/5.20.0/setup.sh

scriptName=$(basename "$0")
echo "${scriptName} set up vars"

# echo "This script is: ${scriptName}"
# echo "My name is: ${myName:?}"
# echo "This platform is: ${platform:?}"
# echo "expandedNodeList: ${expandedNodeList}"
# echo "master: ${masterNode}"
# echo "slaves: ${slaveNodes}"
# echo "nodeID: ${SLURM_NODEID}"
# echo "SLURM_JOB_ID: ${SLURM_JOB_ID}"
# echo "SLURM_PROCID: ${SLURM_PROCID}"