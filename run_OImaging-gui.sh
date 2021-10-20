#!/bin/bash

export JAVA_OPTS="-DRemoteExecutionMode.local=false"

java $JAVA_OPTS -jar target/oimaging-TRUNK-jar-with-dependencies.jar

