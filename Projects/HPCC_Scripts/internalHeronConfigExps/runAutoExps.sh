#!/usr/bin/env bash


# README: This script goes with runAutoExpsTotal. It just runs a full set of trials for with a set of resources

set -x
# Run this on head node of HPC

# shellcheck disable=SC1091
source ./setEnvironmentVars.bash

# Set up arguments
# list of nodes
if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    # exit 1
fi

# Setup useful variables
# shellcheck disable=SC2034
scriptName=$(basename "$0")


echo "HPCC Experiment scripts"

# Cleanup previous executions
rm -f ${MESOS_MASTER_IP}
rm -f ${MESOS_MASTER_PORT}
rm -f ${AURORA_MASTER_IP}
rm -f ${AURORA_MASTER_PORT}
rm -f ${SCRIPT_STATUS}
rm -f ${ZOOKEEPER_ACTIVE_SERVER_LIST}
rm -f ${KAFKA_ACTIVE_SERVER_LIST}
rm -f ${HERON_TRACKER_PORT}

cd ~/hpcc_scripts/
source setEnvironmentVars.bash 
# clusterSlurmJob=$(sbatch --mem=16GB --time=06:30:00 --constraint="IB&xeon&avx2" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=6 --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
# clusterSlurmJob=$(sbatch --mem=16GB --time=06:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=6 --exclude="hpc[3025-3027],hpc[3031-3209],hpc[3211-3264],hpc[3520-3527],hpc35[91-94],hpc[3598-3600],hpc36[48-68],hpc36[70-88],hpc37[66-68],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4388],hpc[4425-4520]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
clusterSlurmJob=$(sbatch --mem=16GB --time=5:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[3025-3027],hpc[3031-3209],hpc[3211-3264],hpc[3520-3527],hpc35[91-94],hpc[3598-3600],hpc36[48-68],hpc36[70-88],hpc37[66-68],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4388],hpc[4425-4520]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
sleep 30

while [ ! -f ${SCRIPT_STATUS} ]; do echo "waiting..."; sleep 5; done; echo "Cluster up! sleeping for 30 seconds";sleep 30; echo -ne '\007'
echo "Cluster ready"

# Setup heron configuration files
./configureHeron.sh

# Start heron-tracker
echo "Starting Heron-tracker"
echo -n "$(python -c 'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1]); s.close()')" >> "${HERON_TRACKER_PORT}"
heron-tracker --port $(cat "$HERON_TRACKER_PORT") </dev/null >/dev/null 2>&1 &
echo "$!" &> heron-tracker.pid
echo "Heron-tracker pid: $(cat heron-tracker.pid)"
echo "Heron-tracker port: $(cat $HERON_TRACKER_PORT)"

sleep 30
cd /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/


# Build topologies

# Run experiments
# bash runExperiments.sh && sleep 30 && touch DONE_EXPS 
# bash runFullExps.sh && sleep 30 && touch DONE_EXPS 

# Store results


# **************************************

# *** largeSetNoDynamo experiments
# # Configure
# sed -i -e "s/BOLT1_PAR=.*/BOLT1_PAR=8;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/BOLT2_PAR=.*/BOLT2_PAR=8;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/CONTAINER_PAR=.*/CONTAINER_PAR=6;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# # Build
# cd ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/
# mvn package -DskipTests=true
# cd -
# # Run
# bash runFullExps.sh && sleep 30 && touch DONE_EXPS 
# # Store results
# squeue -u geoffret | grep $clusterSlurmJob > ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/nodeList
# mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/largeSetDynamo

# # *** largerSetDynamo experiments
# # Configure
# sed -i -e "s/BOLT1_PAR=.*/BOLT1_PAR=16;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/BOLT2_PAR=.*/BOLT2_PAR=16;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/CONTAINER_PAR=.*/CONTAINER_PAR=6;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# # Build
# cd ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/
# mvn package -DskipTests=true
# cd -
# # Run
# bash runFullExps.sh && sleep 30 && touch DONE_EXPS 
# # Store results
# squeue -u geoffret | grep $clusterSlurmJob > ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/nodeList
# mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/largerSetDynamo

# # *** evenLargerSetDynamo experiments
# # Configure
# sed -i -e "s/BOLT1_PAR=.*/BOLT1_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/BOLT2_PAR=.*/BOLT2_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# sed -i -e "s/CONTAINER_PAR=.*/CONTAINER_PAR=5;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# # Build
# cd ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/
# mvn package -DskipTests=true
# cd -
# # Run
# bash runFullExps.sh && sleep 30 && touch DONE_EXPS 
# # Store results
# squeue -u geoffret | grep $clusterSlurmJob > ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/nodeList
# mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetDynamoKafkaStubIsolateMasterZkHighMemHeronInternal

# *** evenLargerSetNoDynamo experiments
# Configure
sed -i -e "s/BOLT1_PAR=.*/BOLT1_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
sed -i -e "s/BOLT2_PAR=.*/BOLT2_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
sed -i -e "s/CONTAINER_PAR=.*/CONTAINER_PAR=5;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# Build
cd ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/
mvn package -DskipTests=true
cd -
# Run
bash runFullExpsNoDynamo.sh && sleep 30 && touch DONE_EXPS 
# Store results
squeue -u geoffret | grep $clusterSlurmJob > ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/nodeList
mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetNoDynamoKafkaStubIsolateMasterZkHighMemHeronInternal

# **************************************




# Clean up jobs
scancel ${clusterSlurmJob}
kill -9 "$(cat ~/hpcc_scripts/heron-tracker.pid)" 
rm ~/hpcc_scripts/heron-tracker.pid

# Cleanup
rm -f ${MESOS_MASTER_IP}
rm -f ${MESOS_MASTER_PORT}
rm -f ${AURORA_MASTER_IP}
rm -f ${AURORA_MASTER_PORT}
rm -f ${SCRIPT_STATUS}
rm -f ${ZOOKEEPER_ACTIVE_SERVER_LIST}
rm -f ${KAFKA_ACTIVE_SERVER_LIST}
rm -f ${HERON_TRACKER_PORT}
