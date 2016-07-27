#!/usr/bin/python3

import subprocess
import os
import sys

print('Master')

NB_NODES = sys.argv[1]

# launch weave
print("Launching weave...")
subprocess.call(['weave', 'launch', '193.51.236.177'])

# set weave env before launching containers
print("Setting weave env...")
process = subprocess.Popen(['weave', 'env'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
env = output.split(' ')[1:]
for e in env:
    name, value = e.split('=')
    if not value.isspace():
        os.environ[name] = value

process = subprocess.Popen(['ifconfig'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
splitted = output.split("\n\n")

for s in splitted:
    if 'enp0s25' not in s:
        continue

    sel = s.split()[6]
    print("IP_MASTER = " + sel.split(':')[1])
    break

# start master container
print("Launching wifidirect-master container...")
subprocess.call(['docker', 'run', '-it', '-e', 'WEAVE_CIDR=10.32.0.42/12', 'rsommerard/wifidirect-master', NB_NODES])
