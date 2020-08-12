#!/usr/bin/env bash
set -x

source ./setEnvironmentVars.bash

RETRY_TIME=5

./startZookeeper.sh

until ./checkZookeeper.sh $ZOOKEEPER_ACTIVE_SERVER_LIST ; do
	echo "Zookeeper not up, sleeping for" ${RETRY_TIME} "seconds"
	sleep ${RETRY_TIME}
	echo
done

./startKafka.sh
sleep 10
./startMesos.sh
sleep 10
./startAurora.sh
echo "up" >> ${SCRIPT_STATUS}

sleep 30
while [ ! -f ${SCRIPT_STATUS} ]; do echo "waiting..."; sleep 5; done; echo "Cluster up! sleeping for 30 seconds";sleep 30; echo -ne '\007'
echo "Cluster ready"

# cd ~/hpcc_scripts/ && ./runExperiments.sh

while true
do
	echo "Holding, press [CTRL+C] to stop.."
	sleep 100
done


# ./stopKafka.sh
# ./stopZookeeper.sh

