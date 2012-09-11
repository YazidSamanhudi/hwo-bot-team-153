#!/bin/sh

JACKSON=jackson-2.0.6/jackson-core-2.0.6.jar:jackson-2.0.6/jackson-annotations-2.0.6.jar:jackson-2.0.6/jackson-databind-2.0.6.jar
LOGBACK=logback-1.0.7/logback-classic-1.0.7.jar:logback-1.0.7/logback-core-1.0.7.jar
SLF4J=slf4j-1.7.0/slf4j-api-1.7.0.jar

java -cp build:$SLF4J:$LOGBACK:$JACKSON shallowgreen.ShallowGreen $*
