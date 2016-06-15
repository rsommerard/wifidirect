#!/bin/bash

ROOT=$(pwd)

cd $ROOT/../wdae-android/WDAELocationTest/
./gradlew clean assembleDebug
cd $ROOT/../wdae-system/WDAENode/docker/
cp $ROOT/../wdae-android/WDAELocationTest/app/build/outputs/apk/app-debug.apk .

cd $ROOT/../wdae-system/WDAENode/
sbt clean universal:packageBin

cd $ROOT/../wdae-system/WDAENode/docker/
cp ../target/universal/wdaenode-1.0.zip .

docker build -t rsommerard/wdaenode .

docker push rsommerard/wdaenode

rm *.zip
rm *.apk

cd ../
sbt clean
