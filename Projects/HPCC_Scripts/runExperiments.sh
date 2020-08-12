
#!/usr/bin/env bash
set -x

cd ~/hpcc_scripts/
source setEnvironmentVars.bash 

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


# *** Test FileDefinedTopology
# Configure
sed -i -e "s/fromMegabytes.*/fromMegabytes(300);/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/topology/GeneralBenchmark.java
sed -i -e "s/BOLT1_PAR=.*/BOLT1_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
sed -i -e "s/BOLT2_PAR=.*/BOLT2_PAR=33;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
sed -i -e "s/CONTAINER_PAR=.*/CONTAINER_PAR=5;/g" ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/src/main/java/workloads/intel/topologies/faultInjected/ExperimentConstants.java
# Build
cd ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/
mvn package -DskipTests=true
cd -
# Run

# while true
# do
# 	echo "Press [CTRL+C] to stop.."
# 	sleep 100
# done


# bash 20190326-testFileDefinedTopology.sh 2>&1 | tee -a "expProg.log" && sleep 30 
# bash 20190413-runModeChangingTrials.sh 2>&1 | tee -a "expProg.log" && sleep 30 
# bash 20190520-utilVaryingExps.sh 2>&1 | tee -a "expProg.log" && sleep 30 
# bash 20190611-dynamicMonitorExps.sh 2>&1 | tee -a "expProg.log" && sleep 30 
bash 20190722-runFailedInstanceTrials.sh 2>&1 | tee -a "expProg.log" && sleep 30 


# Store results
squeue -u geoffret | grep $(cat ${CLUSTER_SLURM_JOB_ID_FILE}) > ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/nodeList
mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentProgress ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults/
# mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/testFunc
mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/experimentResults ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/failedInstanceTests2


# Clean up jobs
kill -9 "$(cat ~/hpcc_scripts/heron-tracker.pid)" 
rm ~/hpcc_scripts/heron-tracker.pid

touch DONE_EXPS 
touch KILL_CLUSTER

while [ ! -f /home/rcf-40/geoffret/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/KILL_CLUSTER ]
do
	# echo "Press [CTRL+C] to stop.."
	sleep 100
done
