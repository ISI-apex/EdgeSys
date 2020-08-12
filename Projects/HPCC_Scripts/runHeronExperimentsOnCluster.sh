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
# clusterSlurmJob=$(sbatch --mem=16GB --time=06:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=6 --exclude="hpc[0965-0972],hpc[0981-1021],hpc[1028-1050],hpc[1123-1128],hpc[1196-1200],hpc[1223-1230],hpc[1118-1122],hpc[1407-1414],hpc[2726-2729],hpc[2758-2761],hpc[3025-3027],hpc[3031-3264],hpc[3520-3527],hpc[3591-3594],hpc[3598-3600],hpc[3648-3688],hpc[3766-3768],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4374],hpc[4433-4520],hpc4522,hpc4523,hpc[4570-4573],hpc[4623-4632],hpc[4657-4674],hpc[4578-4616],hpc-1t-[1-4]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )

# Usual: 

# Generated using script 1129
clusterSlurmJob=$(sbatch --mem=16GB --time=23:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0241-0287], hpc[0289-0496], hpc[0681-0945], hpc[0947-0969], hpc[0971-0972], hpc[0981-1090], hpc[1092-1236], hpc[1253-1396], hpc[2602-2671], hpc[2708-2759], hpc2761, hpc[3001-3264], hpc[3321-3410], hpc[3440-3607], hpc[3609-3768], hpc[3793-3887], hpc[3889-3937], hpc[3940-3946], hpc[3950-3965], hpc[3967-3986], hpc[4009-4056], hpc[4081-4096], hpc[4129-4727], hpc[4729-4752], hpc-1t-[1-4],hpc-lms[22-23]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )



# clusterSlurmJob=$(sbatch --mem=16GB --time=23:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0241-0287], hpc[0289-0496], hpc[0681-0945], hpc[0947-0969], hpc[0971-0972], hpc[0981-1090], hpc[1092-1236], hpc[1253-1396], hpc[2602-2671], hpc[2708-2759], hpc2761, hpc[3001-3264], hpc[3321-3410], hpc[3440-3607], hpc[3609-3768], hpc[3793-3887], hpc[3889-3937], hpc[3940-3946], hpc[3950-3965], hpc[3967-3986], hpc[4009-4056], hpc[4081-4096], hpc[4129-4727], hpc[4729-4752], hpc-1t-[1-4],hpc-lms[22-23]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )



# Update 190401
# clusterSlurmJob=$(sbatch --mem=16GB --time=23:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0965-0969],hpc[0971-0972],hpc[0981-1021],hpc[1028-1050],hpc[1123-1128],hpc[1196-1199],hpc[1223-1230],hpc[1118-1122],hpc[2726-2729],hpc[2758-2759],hpc2761,hpc[3025-3027],hpc[3031-3264],hpc[3520-3527],hpc[3591-3594],hpc[3598-3600],hpc[3648-3688],hpc[3766-3768],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4374],hpc[4433-4520],hpc4522,hpc4523,hpc[4570-4573],hpc[4623-4632],hpc[4657-4674],hpc[4578-4616],hpc-1t-[1-4]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
# clusterSlurmJob=$(sbatch --mem=16GB --time=23:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0965-0972],hpc[0981-1021],hpc[1028-1050],hpc[1123-1128],hpc[1196-1200],hpc[1223-1230],hpc[1118-1122],hpc[1407-1414],hpc[2726-2729],hpc[2758-2761],hpc[3025-3027],hpc[3031-3264],hpc[3520-3527],hpc[3591-3594],hpc[3598-3600],hpc[3648-3688],hpc[3766-3768],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4374],hpc[4433-4520],hpc4522,hpc4523,hpc[4570-4573],hpc[4623-4632],hpc[4657-4674],hpc[4578-4616],hpc-1t-[1-4]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
# clusterSlurmJob=$(sbatch --mem=16GB --time=7:30:00 --constraint="IB&xeon" --cpu-freq=Performance --ntasks-per-node=1 --ntasks=7 --exclude="hpc[0965-0972],hpc[0981-1021],hpc[1028-1050],hpc[1123-1128],hpc[1196-1200],hpc[1223-1230],hpc[1118-1122],hpc[1407-1414],hpc[2726-2729],hpc[2758-2761],hpc[3025-3027],hpc[3031-3264],hpc[3520-3527],hpc[3591-3594],hpc[3598-3600],hpc[3648-3688],hpc[3766-3768],hpc[3606-3607],hpc[3817-3834],hpc3852,hpc[4129-4176],hpc[4323-4324],hpc[4331-4374],hpc[4433-4520],hpc4522,hpc4523,hpc[4570-4573],hpc[4623-4632],hpc[4657-4674],hpc[4578-4616],hpc-1t-[1-4]" --mail-type=BEGIN,END "scriptToRun.sh"  | awk '{ print $4 }' )
echo "${clusterSlurmJob}" > "${CLUSTER_SLURM_JOB_ID_FILE}"
sleep 30

# while [ ! -f ${SCRIPT_STATUS} ]; do echo "waiting..."; sleep 5; done; echo "Cluster up! sleeping for 30 seconds";sleep 30; echo -ne '\007'
# echo "Cluster ready"

# # Setup heron configuration files
# ./configureHeron.sh

# # Start heron-tracker
# echo "Starting Heron-tracker"
# echo -n "$(python -c 'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1]); s.close()')" >> "${HERON_TRACKER_PORT}"
# heron-tracker --port $(cat "$HERON_TRACKER_PORT") </dev/null >/dev/null 2>&1 &
# echo "$!" &> heron-tracker.pid
# echo "Heron-tracker pid: $(cat heron-tracker.pid)"
# echo "Heron-tracker port: $(cat $HERON_TRACKER_PORT)"

# sleep 30
# cd /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/


# Build topologies

# Run experiments
# bash runExperiments.sh && sleep 30 && touch DONE_EXPS 
# bash runFullExps.sh && sleep 30 && touch DONE_EXPS 

# Store results

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
