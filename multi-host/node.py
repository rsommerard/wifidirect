#!/usr/bin/python3

import subprocess
import os
import sys

print('Node')

IP_MASTER = sys.argv[1]
NB_NODES = sys.argv[2]

print("IP_MASTER = " + str(IP_MASTER))
print("NB_NODES = " + str(NB_NODES))

package_activity_name = 'fr.inria.rsommerard.widitestingproject/.MainActivity'

if not os.path.exists('containers.info'):
    print('The containers.info file does not exist.')
    sys.exit(1)

if not os.path.exists('log'):
    os.mkdir('log')

# launch weave
print("Launching weave...")
subprocess.call(['weave', 'launch', IP_MASTER])

# set weave env before launching containers
print("Setting weave env...")
process = subprocess.Popen(['weave', 'env'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
env = output.split(' ')[1:]
for e in env:
    name, value = e.split('=')
    if not value.isspace():
        os.environ[name] = value

# start node containers
print("Launching wifidirect-node containers...")
for i in range(NB_NODES):
    process = subprocess.Popen(['docker', 'run', '-d', '--privileged', 'rsommerard/wifidirect-node', package_activity_name], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

    with open('containers.info', 'a+') as f:
        f.write("Node=" + output)

    print('Node ' + str(i + 1) + ' launched...')
