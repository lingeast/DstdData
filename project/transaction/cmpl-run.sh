#!/bin/sh

make clean
make server
make client
rmiregistry -J-classpath -J.. 4444 &

cd ../test.part1
export CLASSPATH=".:gnujaxp.jar"
javac RunTests.java
java -ea -DrmiPort=4444 RunTests MASTER.xml
pidof rmiregistry
