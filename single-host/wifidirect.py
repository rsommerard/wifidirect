import os
import subprocess
import shutil
import time

# TODO: CLI args nb emulators
# TODO: CLI arg build or not
# TODO: CLI arg master path
# TODO: CLI arg node path

NB_NODES = 2

ROOT = os.getcwd()

MASTER = '/home/romain/Lab/wifidirect/system/Master/docker'
NODE = '/home/romain/Lab/wifidirect/system/Node/docker'

# check and install weave or reset if already installed
weave = shutil.which('weave')

if weave == None:
    print('Installing weave...')
    subprocess.call(['sudo', 'curl', '-L', 'git.io/weave', '-o', '/usr/local/bin/weave'])
    subprocess.call(['sudo', 'chmod', '+x', '/usr/local/bin/weave'])
else:
    print('Reseting weave...')
    subprocess.call(['weave', 'reset'])

# remove old containers
process = subprocess.Popen(['docker', 'ps', '-a'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
cont = output.split('\n')[1:]

for c in cont:
    if not c:
        continue

        print('Removing ' + container_name + '...')

    container_id = c.split()[0]
    container_name = c.split()[1]

    if ('rsommerard/wifidirect-master' in container_name) or ('rsommerard/wifidirect-node' in container_name):
        subprocess.call(['docker', 'kill', container_id])
        subprocess.call(['docker', 'rm', '-f', container_id])

# build containers
print('Building wifidirect-master...')
os.chdir(MASTER)
# subprocess.call(['python3', 'build.py'])

print('Building wifidirect-node...')
os.chdir(NODE)
# subprocess.call(['python3', 'build.py'])

os.chdir(ROOT)

# launch master script
print('Launching master script...')
subprocess.Popen(['gnome-terminal', '--working-directory', ROOT, '-e', 'python3 master.py'])

# waiting master container
print('Waiting master container...')
process = subprocess.Popen(['docker', 'ps'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while 'rsommerard/wifidirect-master' not in output:
    time.sleep(3)
    process = subprocess.Popen(['docker', 'ps'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

# launch nodes script
print('Launching node scripts...')
for i in range(0, NB_NODES):
    time.sleep(3)
    subprocess.Popen(['gnome-terminal', '--working-directory', ROOT, '-e', 'python3 node.py'])
