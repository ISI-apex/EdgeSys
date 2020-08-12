#!/usr/bin/env bash




if [ "$#" -ne 1 ]; then
    echo "Wrong number of parameters"
    exit 1
fi


OLDIFS=$IFS
IFS=,
for i in $(cat $1); do
	OLDIFS2=$IFS
	IFS=:
	tt=( $i )
	# echo "${tt[0]}"
	if [[ "imok" != $(echo ruok | nc ${tt[@]}) ]]; then
		echo "${tt[@]} is not up yet"
		exit 1
	fi
	IFS=$OLDIFS2
done
IFS=$OLDIFS
exit 0