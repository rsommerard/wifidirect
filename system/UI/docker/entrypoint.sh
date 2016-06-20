#!/bin/bash

IP=$(ip addr list eth0 | grep 'inet ' | cut -d ' ' -f6 | cut -d/ -f1)
echo "Configuring redir for $IP..."
redir --laddr=$IP --lport=8080 --caddr=127.0.0.1 --cport=8080 &

./wifidirect-ui-1.0/bin/wifidirect-ui
