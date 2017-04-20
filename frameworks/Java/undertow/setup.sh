#!/bin/bash

fw_depends java maven

mvn clean package

java $JAVA_OPTS_TFB -jar target/hello-undertow.jar $UNDERTOW_ARGS
