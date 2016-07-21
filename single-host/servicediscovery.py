import subprocess
import os
import sys

print('ServiceDiscovery')

# set weave env before launching containers
print("Setting weave env...")
process = subprocess.Popen(['weave', 'env'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
env = output.split(' ')[1:]
for e in env:
    name, value = e.split('=')
    if not value.isspace():
        os.environ[name] = value

# start master container
print("Launching wifidirect-servicediscovery container...")
process = subprocess.Popen(['docker', 'run', '-d', '-e', 'WEAVE_CIDR=10.32.0.43/12', 'rsommerard/wifidirect-servicediscovery'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

with open('containers.info', 'a+') as f:
    f.write("ServiceDiscovery=" + output)
