import subprocess
import os
import sys

print('Master')

nb_nodes = sys.argv[1]

# launch weave
print("Launching weave...")
subprocess.call(['weave', 'launch'])

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
print("Launching wifidirect-master container...")
subprocess.call(['docker', 'run', '--rm', '-it', '-e', 'WEAVE_CIDR=10.32.0.42/12', 'rsommerard/wifidirect-master', nb_nodes])
