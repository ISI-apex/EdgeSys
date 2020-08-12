#!/usr/bin/env bash

# Do not use:
	# project home: /home/rcf-proj2/jnw/geoffret
	# staging: /staging/jnw/geoffret

# Okay to use:
	# scratch: /scratch/geoffret
	# scratch2: /scratch/geoffret

# Libs: /scratch/geoffret/edgesysHPCC/lib
# prefix: /scratch/geoffret/edgesysHPCC


# exit 1

source setEnvironmentVars.bash
# export TMP_PREFIX="/scratch2/geoffret/edgesysHPCC"
# export TMP_LIBS_DIR="${TMP_PREFIX}/lib"
# export TMP_BUILD_DIR="${TMP_PREFIX}/buildFiles"

mkdir $TMP_BUILD_DIR
mkdir $TMP_LIBS_DIR

cd $TMP_BUILD_DIR || exit 1
source /usr/usc/java/1.8.0_45/setup.sh
export MY_PREFIX=${TMP_PREFIX}
export PATH="${TMP_PREFIX}/bin":${PATH}
export PATH="${TMP_BUILD_DIR}/apache-maven-3.5.2/bin":${PATH}
export SASL_PATH="${TMP_LIBS_DIR}/sasl2"

export CC=/usr/bin/gcc
export CCX=/usr/bin/g++
source /usr/usc/python/2.7.8/setup.sh
source /usr/usc/gnu/autoconf/2.69/setup.sh
source /usr/usc/perl/5.20.0/setup.sh


# libtool
wget http://ftpmirror.gnu.org/libtool/libtool-2.4.6.tar.gz
tar -zxvf libtool-2.4.6.tar.gz
(
	cd libtool-2.4.6 || exit 1
	./configure --prefix=${MY_PREFIX}
	make
	make install
	touch $TMP_BUILD_DIR/DONE_LIBTOOL
)





# exit 1





# apr
wget https://archive.apache.org/dist/apr/apr-1.6.3.tar.gz
tar -zxvf apr-1.6.3.tar.gz
(
	cd apr-1.6.3 || exit 1
	touch libtoolT
	./configure --prefix=${MY_PREFIX}
	make
	make install
	touch $TMP_BUILD_DIR/DONE_APR
)

# apr-util
wget https://ftp.wayne.edu/apache//apr/apr-util-1.6.1.tar.gz
tar -zxvf apr-util-1.6.1.tar.gz
(
	cd apr-util-1.6.1 || exit 1
	./configure --prefix=${MY_PREFIX} --with-apr=${MY_PREFIX}/
	make 
	make install
	touch $TMP_BUILD_DIR/DONE_APRUTIL
)

# maven
wget https://archive.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
tar -zxvf apache-maven-3.5.2-bin.tar.gz
(
	cd apache-maven-3.5.2 || exit 1
	export PATH="${TMP_BUILD_DIR}/apache-maven-3.5.2/bin":${PATH}
	touch $TMP_BUILD_DIR/DONE_MAVEN
)

# libsasl
wget https://github.com/cyrusimap/cyrus-sasl/releases/download/cyrus-sasl-2.1.26/cyrus-sasl-2.1.26.tar.gz
tar -zxvf cyrus-sasl-2.1.26.tar.gz
(
	cd cyrus-sasl-2.1.26 || exit 1
	./configure --prefix=${MY_PREFIX}
	make
	make install
	touch $TMP_BUILD_DIR/DONE_LIBSASL
		# *******************ln -s ${MY_PREFIX}/lib/sasl2 /usr/lib/sasl2
)

# subversion
wget https://archive.apache.org/dist/subversion/subversion-1.9.7.tar.gz
tar -zxvf subversion-1.9.7.tar.gz 
(
	cd subversion-1.9.7 || exit 1
	wget http://www.sqlite.org/sqlite-amalgamation-3071501.zip
	unzip sqlite-amalgamation-3071501.zip 
	mv sqlite-amalgamation-3071501 sqlite-amalgamation
	./configure --prefix=${MY_PREFIX} --with-apr=${MY_PREFIX}/ --with-apr-util=${MY_PREFIX}/
	make 
	make install
	touch $TMP_BUILD_DIR/DONE_SVN
)

# Mesos
wget http://archive.apache.org/dist/mesos/1.0.4/mesos-1.0.4.tar.gz
tar -zxvf mesos-1.0.4.tar.gz
(
	cd mesos-1.0.4 || exit 1
	mkdir build
	cd build || exit 1
	../configure --prefix=${MY_PREFIX} --with-apr=${MY_PREFIX}/ --with-sasl=${MY_PREFIX}/ --with-svn=${MY_PREFIX}/
	make -j 4
	make check GTEST_FILTER=-OsTest.User
	make install
	touch $TMP_BUILD_DIR/DONE_MESOS
)

# Netcat
wget https://downloads.sourceforge.net/project/netcat/netcat/0.7.1/netcat-0.7.1.tar.gz
tar -zxvf netcat-0.7.1.tar.gz
(
	cd netcat-0.7.1 || exit 1
	./configure --prefix=${MY_PREFIX}
	make
	make install
	touch $TMP_BUILD_DIR/DONE_NETCAT
)

# Zookeeper
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz
tar -zxvf zookeeper-3.4.10.tar.gz
(
	cd zookeeper-3.4.10 || exit 1
	# vi conf/zoo.cfg
	# tickTime=2000
	# dataDir=/staging/jnw/geoffret/dataDirs/zookeeper
	# clientPort=2181
	# bin/zkServer.sh start
	# bin/zkCli.sh -server localhost:2181
	# bin/zkServer.sh stop
	touch $TMP_BUILD_DIR/DONE_ZOOKEEPER
)

# kafka
wget https://archive.apache.org/dist/kafka/0.10.0.1/kafka_2.11-0.10.0.1.tgz
tar -zxvf kafka_2.11-0.10.0.1.tgz
(
	cd kafka_2.11-0.10.0.1 || exit 1
	# vi config/server.properties
	# Used to launch server
	# log.dirs=/tmp/kafka-logs
	# zookeeper.connect=localhost:2181
	# delete.topic.enable=true
	# *** For multiple brokers
	# *** - Unique broker.id
	# *** - Unique server port
	# *** - unique log.dirs
	# *** Launch each with kafka-server-start.sh CONF
	# vi config/zookeeper.properties
	# Used for zookeeper scripts, can launch manually
	# dataDir=/tmp/zookeeper

	# bin/zookeeper-server-start.sh config/zookeeper.properties
	touch $TMP_BUILD_DIR/DONE_KAFKA
)

# automake
curl -O ftp://ftp.gnu.org/gnu/automake/automake-1.15.1.tar.gz
tar -zxvf automake-1.15.1.tar.gz
(
	cd automake-1.15.1 || exit 1
	./configure --prefix=${MY_PREFIX} 
	make
	make install
	touch $TMP_BUILD_DIR/DONE_AUTOMAKE
)


# aurora
(
	cp installBinaries/aurora-0.14.0.zip $TMP_BUILD_DIR/
	unzip aurora-0.14.0.zip
	touch $TMP_BUILD_DIR/DONE_AURORA
)



# dstat
# wget https://github.com/dagwieers/dstat/archive/0.7.3.tar.gz
# tar -zxvf 0.7.3.tar.gz
# cd dstat-0.7.3

# protocolbuffers
wget https://github.com/google/protobuf/releases/download/v2.5.0/protobuf-2.5.0.tar.gz
tar -zxvf protobuf-2.5.0.tar.gz
(
	cd protobuf-2.5.0 || exit 1
	./configure --prefix=${MY_PREFIX} 
	make
	make install
	touch $TMP_BUILD_DIR/DONE_PROTOBUF
)



# HPCC scripts
git clone https://github.com/LLNL/magpie.git
(
	touch $TMP_BUILD_DIR/DONE_MAGPIE

)


# Heron from binaries


# Heron 0.17.8 from binaries
cd ${TMP_BUILD_DIR} || exit 1
mkdir heron
cd heron
wget https://github.com/apache/incubator-heron/releases/download/0.17.8/heron-install-0.17.8-centos.sh
chmod +x heron-*.sh
./heron-install-0.17.8-centos.sh --prefix=${MY_PREFIX}



exit 1



cd /staging/jnw/geoffret/heron;mv conf confOld;ln -s /scratch/geoffret/edgesysHPCC/dynamo-data-github/dynamic_fault_tolerance/configurations/heron-0.17.8/ conf; cd -

# Finish Heron setup
# This is needed because the slurm scheduler gets the paths wrong, so we
# extract the files ourselves and put them where Heron is looking
mv ${MY_PREFIX}/heron/conf ${MY_PREFIX}/heron/confOld
ln -s /scratch/geoffret/edgesysHPCC/dynamo-data-github/dynamic_fault_tolerance/configurations/heron/conf ${MY_PREFIX}/heron/conf
mv ${MY_PREFIX}/herontools/conf ${MY_PREFIX}/herontools/confOld
ln -s /home/rcf-proj2/jnw/geoffret/dynamo-data-github/dynamic_fault_tolerance/configurations/herontools/conf ${MY_PREFIX}/herontools/conf
(
	ORIG_HERON_BIN=../bin
	ORIG_HERON_LIB=../lib
	cd /staging/jnw/geoffret/heron/dist
	tar -zxvf heron-core.tar.gz
	rm release.yaml

	cp heron-core/bin/heron-downloader "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-downloader.sh "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-executor "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-python-instance "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-shell "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-stmgr "${ORIG_HERON_BIN}"
	cp heron-core/bin/heron-tmaster "${ORIG_HERON_BIN}"

	cp -r heron-core/lib/ckptmgr "${ORIG_HERON_LIB}"
	cp -r heron-core/lib/downloaders "${ORIG_HERON_LIB}"
	cp -r heron-core/lib/instance "${ORIG_HERON_LIB}"
	cp -r heron-core/lib/metricscachemgr "${ORIG_HERON_LIB}"
	cp -r heron-core/lib/metricsmgr "${ORIG_HERON_LIB}"
	cp -r heron-core/lib/statefulstorage "${ORIG_HERON_LIB}"

	rm -rf heron-core/
)

# Install kafka python for experiments
pip install --user kafka-python==1.3.5
pip install --user requests==2.18.4
exit 0







# Heron test
heron submit local \
  /staging/jnw/geoffret/heron/examples/heron-examples.jar \
  com.twitter.heron.examples.ExclamationTopology \
  ExclamationTopology \
  --deploy-deactivated
heron activate local ExclamationTopology
heron deactivate local ExclamationTopology
heron kill local ExclamationTopology

heron submit localHPCC \
  /staging/jnw/geoffret/heron/examples/heron-examples.jar \
  com.twitter.heron.examples.AckingTopology \
  AckingTopology \
  --deploy-deactivated
heron activate localHPCC AckingTopology
heron deactivate localHPCC AckingTopology
heron kill localHPCC AckingTopology

heron submit slurmHPCC \
  /staging/jnw/geoffret/heron/examples/heron-examples.jar \
  com.twitter.heron.examples.AckingTopology \
  AckingTopology \
  --deploy-deactivated
heron activate slurmHPCC AckingTopology
heron deactivate slurmHPCC AckingTopology
heron kill slurmHPCC AckingTopology




# https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/6.2.1/html/installation_on_jboss_eap/install_maven



