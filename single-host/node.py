import subprocess
import os
import sys

print('Node')

package_activity_name = sys.argv[1]

if not os.path.exists('containers.info'):
    print('The containers.info file does not exist.')
    sys.exit(1)

if not os.path.exists('log'):
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

# start node container
print("Launching wifidirect-node container...")
process = subprocess.Popen(['docker', 'run', '-d', '--privileged', 'rsommerard/wifidirect-node', package_activity_name], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

with open('containers.info', 'a+') as f:
    f.write("Node=" + output)
