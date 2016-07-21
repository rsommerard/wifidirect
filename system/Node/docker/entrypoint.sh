#!/bin/bash

echo $1

ME=$(ip route | grep 'src 10.' | awk '{print $NF;exit}')

echo "IP: $ME"

IP=$(ip addr list eth0 | grep 'inet ' | cut -d' ' -f6 | cut -d/ -f1)
echo "Configuring redir for $ME..."
redir --laddr=$ME --lport=11131 --caddr=127.0.0.1 --cport=11131 &

./wifidirect-node-1.0/bin/wifidirect-node $1 $ME > /widi/log/${ME}_server.log &

echo 'Starting emulator[5554]...'
emulator64-x86 -avd AndroidAPI22x86 -no-skin -no-audio -no-window -no-boot-anim -noskin -gpu off -port 5554 &

echo 'Waiting for emulator to start...'
BOOT_COMPLETED=''
FAIL_COUNTER=0
until [[ "$BOOT_COMPLETED" =~ '1' ]]; do
   BOOT_COMPLETED=`adb -s emulator-5554 shell getprop sys.boot_completed 2>&1`
   if [[ "$BOOT_COMPLETED" =~ 'not found' ]]; then
      let 'FAIL_COUNTER += 1'
      if [[ $FAIL_COUNTER -gt 120 ]]; then
        echo '  Failed to start emulator'
        exit 1
      fi
   fi
   sleep 1
done

echo 'Emulator started'

echo 'Adding emulator redirections...'
echo "" > ~/.emulator_console_auth_token
(echo 'auth ""'; sleep 1; echo "redir add tcp:11131:11131"; sleep 1; echo 'exit') | telnet localhost 5554

echo 'Installing the apk...'
adb -e install -r /app-debug.apk
adb -e logcat -c

echo 'Launching application...'
adb -e shell am start -n $1

echo 'Running...'
adb -e logcat | grep WiDi > /widi/log/${ME}_emulator.log
