#!/bin/sh

make clean
make server
make client

cd ../test.part1
export CLASSPATH=".:gnujaxp.jar"
javac RunTests.java
java -DrmiPort=4444 RunTests MASTER.xml
