#!/bin/bash

EPORT=$1
RPORT=$2

echo "" > ~/.emulator_console_auth_token
(echo 'auth ""'; sleep 1; echo "redir add tcp:$RPORT:$RPORT"; sleep 1; echo 'exit') | telnet localhost $EPORT
