#!/usr/bin/env bash

# README: This script goes with runAutoExps. It changes a set of parameters at the Heron level, then launches a set of experiments. In this case, it varies the heron-internal parameters.


set -x

cd ~/hpcc_scripts
source ./setEnvironmentVars.bash

main() {
	CONFIGS=(18)
	NUM=1
	for CONFIG in "${CONFIGS[@]}"
	do
		cp heron-internal-configs/heron_internals-${CONFIG}.yaml /home/rcf-proj2/jnw/geoffret/dynamo-data-github/dynamic_fault_tolerance/configurations/heron/conf/exampleHPCC/heron_internals.yaml
		# echo heron-internal-configs/heron_internals-${CONFIG}.yaml
		# Run Trial
		./runAutoExps.sh
		# Rename results
		renameResults
	done

	# CONFIGS=(4 5 6 7 8 9)
	# NUM=3
	# for CONFIG in "${CONFIGS[@]}"
	# do
	# 	cp heron-internal-configs/heron_internals-${CONFIG}.yaml /home/rcf-proj2/jnw/geoffret/dynamo-data-github/dynamic_fault_tolerance/configurations/heron/conf/exampleHPCC/heron_internals.yaml
	# 	# echo heron-internal-configs/heron_internals-${CONFIG}.yaml
	# 	# Run Trial
	# 	./runAutoExps.sh
	# 	# Rename results
	# 	renameResults
	# done

	# CONFIGS=(2)
	# NUM=6
	# for CONFIG in "${CONFIGS[@]}"
	# do
	# 	cp heron-internal-configs/heron_internals-${CONFIG}.yaml /home/rcf-proj2/jnw/geoffret/dynamo-data-github/dynamic_fault_tolerance/configurations/heron/conf/exampleHPCC/heron_internals.yaml
	# 	# echo heron-internal-configs/heron_internals-${CONFIG}.yaml
	# 	# Run Trial
	# 	./runAutoExps.sh
	# 	# Rename results
	# 	renameResults
	# done
}

renameResults() {
	# echo evenLargerSetDynamoKafkaStubIsolateMasterZkHighMemHeronInternal${CONFIG}-${NUM}
	# echo evenLargerSetNoDynamoKafkaStubIsolateMasterZkHighMemHeronInternal${CONFIG}-${NUM}
	mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetDynamoKafkaStubIsolateMasterZkHighMemHeronInternal ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetDynamoKafkaStubIsolateMasterZkHighMemHeronInternal${CONFIG}-${NUM}
	mv ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetNoDynamoKafkaStubIsolateMasterZkHighMemHeronInternal ~/dynamoProjDir/dynamo-data-github/dynamic_fault_tolerance/benchmarks/dynamo_v2/bin/evenLargerSetNoDynamoKafkaStubIsolateMasterZkHighMemHeronInternal${CONFIG}-${NUM}
}




main
exit $?