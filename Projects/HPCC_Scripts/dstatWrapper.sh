#!/usr/bin/env bash
nohup /home/rcf-40/geoffret/dynamoProjDir/buildFiles2/dstat-0.7.3/dstat -T --cpu-adv --cpu-use -d -g -i -l --mem-adv -n -p -r -s -y --fs --socket --tcp --udp --unix  --disk-svctm --top-bio-adv --top-cputime --top-io-adv --top-latency --noheaders --output dstat.out 1  </dev/null >/dev/null 2>&1 &
exec "$@"