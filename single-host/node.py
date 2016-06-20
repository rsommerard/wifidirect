import subprocess
import os
import sys

print('Node')

package_activity_name = sys.argv[1]

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
subprocess.call(['docker', 'run', '--rm', '-it', '--privileged', 'rsommerard/wifidirect-node', package_activity_name])
