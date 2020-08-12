#!/usr/bin/env bash
set -x
# Launch this with one instance

startAurora="yes"
runObserverOnMasterNode="no"

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

MESOS_LOCAL_PATH=${TMPDIR}/mesos
AURORA_DATA_PATH=${TMPDIR}/aurora
AURORA_LOCAL_DIST_PATH=${TMPDIR}/aurora-dist

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

masterPort=8081

echo "master: ${masterNode}"
echo "agents: ${slaveNodes}"


if [[ "${startAurora}" == 'yes' ]]; then
	rm -rf $AURORA_MASTER_IP
	rm -rf $AURORA_MASTER_PORT

	# Start scheduler on master node
	for nodeToSetup in ${masterNode}; do
		echo "Setting up $nodeToSetup as master"
		nodeToSetup="$nodeToSetup-e0"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')
		masterIp=${tempIp}

		# Create working directory
		ssh "${nodeToSetup}" "mkdir -p $AURORA_DATA_PATH/var/lib/aurora/"
		ssh "${nodeToSetup}" "mkdir -p $AURORA_DATA_PATH/var/log/aurora/"
		ssh "${nodeToSetup}" "mkdir -p $AURORA_DATA_PATH/var/lib/aurora/scheduler/db"

		# Copy dist to remote node
		rsync -apv ${AURORA_DIST_PATH}/ ${nodeToSetup}:${AURORA_LOCAL_DIST_PATH}

		# Initialize log
		ssh "${nodeToSetup}" "source ${TMP_PREFIX}/hpcc_scripts/setEnvironmentVars.bash;mesos-log initialize --path=$AURORA_DATA_PATH/var/lib/aurora/scheduler/db"

		echo "Starting aurora-scheduler  on ${nodeToSetup}"
		# Messy because we have a mix of needing to set env variables on remote
		#  end and substitute from the shell
		ssh "${nodeToSetup}" 'export GLOG_v=0; export LIBPROCESS_PORT=8083 '"; export MESOS_NATIVE_JAVA_LIBRARY=${TMP_PREFIX}/lib/libmesos.so; export JAVA_OPTS=\"-server -Djava.library.path=$AURORA_LOCAL_DIST_PATH/scheduler/usr/lib/aurora/lib\"""; nohup $AURORA_LOCAL_DIST_PATH/scheduler/usr/lib/aurora/bin/aurora-scheduler -cluster_name=${CLUSTER_NAME} -http_port=8081 -mesos_master_address=\"$(cat ${MESOS_MASTER_IP}):$(cat ${MESOS_MASTER_PORT})\" -zk_endpoints=\"$(cat $ZOOKEEPER_ACTIVE_SERVER_LIST)\" -serverset_path=/aurora/scheduler -allowed_container_types=DOCKER,MESOS -native_log_quorum_size=1 -native_log_zk_group_path=/aurora/replicated-log -native_log_file_path=$AURORA_DATA_PATH/var/lib/aurora/scheduler/db -backup_dir=$AURORA_DATA_PATH/var/lib/aurora/scheduler/backups -thermos_executor_path=$AURORA_LOCAL_DIST_PATH/executor/usr/bin/thermos_executor -thermos_executor_flags=\"--execute-as-user=geoffret --nosetuid\" </dev/null >/dev/null 2>&1 &"
		echo "Status for ${nodeToSetup}: ${?}"
		sleep 1

		# Start observer
		if [[ "${runObserverOnMasterNode}" == 'yes' ]]; then
			echo "Also launching thermos_observer on master"
			ssh "$nodeToSetup" "nohup $AURORA_LOCAL_DIST_PATH/executor/usr/bin/thermos_observer --port=1338 --mesos-root=$MESOS_LOCAL_PATH/${nodeToSetup} </dev/null >/dev/null 2>&1 &"
			sleep 1
		fi

	done


	# Start observers
	for nodeToSetup in ${slaveNodes}; do
		echo "Setting up $nodeToSetup observer"
		nodeToSetup="$nodeToSetup-e0"
		tempIp=$(getent hosts "${nodeToSetup}" | awk '{ print $1 }')

		# Copy dist to remote node
		rsync -apv ${AURORA_DIST_PATH}/ ${nodeToSetup}:${AURORA_LOCAL_DIST_PATH}

		echo "Starting thermos_observer on ${nodeToSetup}"
		ssh "$nodeToSetup" "nohup $AURORA_LOCAL_DIST_PATH/executor/usr/bin/thermos_observer --port=1338 --mesos-root=$MESOS_LOCAL_PATH/${nodeToSetup} </dev/null >/dev/null 2>&1 &"
		echo "Status for ${nodeToSetup}: ${?}"
		sleep 1
	done

	echo -n "${masterIp}" >> "${AURORA_MASTER_IP}"
	echo -n "${8081}" >> "${AURORA_MASTER_PORT}"

	# Setup client parameters
	# TODO: Zookeeper settings in client
	# $AURORA_DIST_PATH/tools/etc/aurora/clusters.json
	# cp $AURORA_DIST_PATH/tools/etc/aurora/clusters-template.json $AURORA_DIST_PATH/tools/etc/aurora/clusters.json
	# sed -i -e "s/ZOOKEEPERSTRING/$(cat "${ZOOKEEPER_ACTIVE_SERVER_LIST}")/g" "$AURORA_DIST_PATH/tools/etc/aurora/clusters.json"
	python updateZkServer.py $AURORA_DIST_PATH/tools/etc/aurora/clusters.json ${CLUSTER_NAME} "$(cat "${ZOOKEEPER_ACTIVE_SERVER_LIST}")"
fi


