#!/bin/sh

if [ ! -d build ]; then
	mkdir build
fi

javac -cp google-gson-2.2.2/gson-2.2.2.jar -d build `find src -name "*.java" -type f -print`
