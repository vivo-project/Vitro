#!/bin/sh

if  [ -z "$1" ]
  then
    echo 'New version number required eg. 1.9.0-rc1'
    exit 1
fi

mvn -T 1C versions:set -DgenerateBackupPoms=false -DnewVersion=$1
cd installer
mvn -T 1C versions:set -DgenerateBackupPoms=false -DnewVersion=$1
cd ..
