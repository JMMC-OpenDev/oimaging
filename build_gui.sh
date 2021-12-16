#!/bin/bash

set -eux

DIR=`pwd`
# 1. Dependencies:
cd $DIR/build

# Install parent-pom
cd jmcs/parent-pom
# see README.md
mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
cd $DIR/build

# Build all modules (skip test)
MVN_OPTS="-Djarsigner.skip=true -Dmaven.javadoc.skip=true -Dmaven.test.skip=true"

# set -ux

for mod in jmcs jmcs/testgui oitools jmal oiexplorer-core
do
  cd $mod
  mvn process-resources
  mvn $MVN_OPTS clean install
  cd -
done

echo "Build Dependencies: done."


# 2. build module:
cd $DIR
# note: use insecure https as restlet https certificates are out-dated!
mvn -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true process-resources
mvn -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true $MVN_OPTS clean install

echo "Build: done."

