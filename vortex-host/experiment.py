#!/usr/bin/python3

import subprocess
import os
import sys
import shutil
import time

print('Node')

NB_NODES = sys.argv[1]

print("NB_NODES = " + str(NB_NODES))

package_activity_name = 'fr.inria.rsommerard.widitestingproject/.MainActivity'

if os.path.exists('containers.info'):
    os.remove('containers.info')

if os.path.exists('log'):
    shutil.rmtree('log')

os.mkdir('log')

# set weave env before launching containers
print("Setting weave env...")
process = subprocess.Popen(['weave', 'env'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
env = output.split(' ')[1:]
for e in env:
    name, value = e.split('=')
    if not value.isspace():
        os.environ[name] = value

# start servicediscovery container
print("Launching wifidirect-servicediscovery container...")
process = subprocess.Popen(['docker', 'run', '-d', '-e', 'WEAVE_CIDR=10.32.0.43/12', 'rsommerard/wifidirect-servicediscovery'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

with open('containers.info', 'a+') as f:
    f.write("ServiceDiscovery=" + output)

time.sleep(3)

# start ui container
print("Launching wifidirect-ui container...")
process = subprocess.Popen(['docker', 'run', '-d', '-p', '3012:8080', 'rsommerard/wifidirect-ui'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

with open('containers.info', 'a+') as f:
    f.write("UI=" + output)

time.sleep(3)

# start node containers
print("Launching wifidirect-node containers...")
for i in range(int(NB_NODES)):
    time.sleep(3)
    process = subprocess.Popen(['docker', 'run', '-d', '--privileged', 'rsommerard/wifidirect-node', package_activity_name], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

    with open('containers.info', 'a+') as f:
        f.write("Node=" + output)

print(str(i + 1) + ' nodes launched.')
