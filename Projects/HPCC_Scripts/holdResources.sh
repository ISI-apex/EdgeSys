#!/usr/bin/env bash
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
rm -f ${CLUSTER_SLURM_JOB_ID_FILE}
rm -f /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/DONE_EXPS
rm -f /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/KILL_CLUSTER


cd ~/hpcc_scripts/
source setEnvironmentVars.bash 
# clusterSlurmJob=$(sbatch --mem=16GB --time=06:30:00 --constraint="IB&xeon&avx2" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=6 --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )

# Usual: 

# Generated using script
clusterSlurmJob=$(sbatch --mem=16GB --time=23:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0241-0287], hpc[0289-0496], hpc[0681-0945], hpc[0947-0969], hpc[0971-0972], hpc[0981-1090], hpc[1092-1236], hpc[1253-1396], hpc[2602-2671], hpc[2708-2759], hpc2761, hpc[3001-3264], hpc[3321-3410], hpc[3440-3607], hpc[3609-3768], hpc[3793-3887], hpc[3889-3937], hpc[3940-3946], hpc[3950-3965], hpc[3967-3986], hpc[4009-4056], hpc[4081-4096], hpc[4129-4727], hpc[4729-4752], hpc-1t-[1-4],hpc-lms[22-23]" --mail-type=BEGIN,END "scriptToHold.sh"  | awk '{ print $4 }' )


echo "${clusterSlurmJob}" > "${CLUSTER_SLURM_JOB_ID_FILE}"
sleep 30


echo "Waiting for script to finish"
# while [ ! -f /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/DONE_EXPS ]
while [ ! -f /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/KILL_CLUSTER ]
do
	echo "Press [CTRL+C] to stop.."
	sleep 100
done







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
rm -f ${CLUSTER_SLURM_JOB_ID_FILE}
