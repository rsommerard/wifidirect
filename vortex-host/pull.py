#!/usr/bin/python3

import subprocess
import os
import sys

print('Pulling rsommerard/wifidirect-master images....')
subprocess.call(['docker', 'pull', 'rsommerard/wifidirect-master'])

print('Pulling rsommerard/wifidirect-node images....')
subprocess.call(['docker', 'pull', 'rsommerard/wifidirect-node'])

print('Pulling rsommerard/wifidirect-servicediscovery images....')
subprocess.call(['docker', 'pull', 'rsommerard/wifidirect-servicediscovery'])

print('Pulling rsommerard/wifidirect-ui images....')
subprocess.call(['docker', 'pull', 'rsommerard/wifidirect-ui'])
