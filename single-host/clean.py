import os
import subprocess
import shutil

CWD = os.getcwd()
ROOT = str.join('/', CWD.split('/')[:-1])
MASTER = ROOT + '/system/Master'
NODE = ROOT + '/system/Node'
UI = ROOT + '/system/UI'

print('Cleaning Master...')
os.chdir(MASTER + "/docker")
subprocess.call(['python3', 'clean.py'])

print('Cleaning Node...')
os.chdir(NODE + "/docker")
subprocess.call(['python3', 'clean.py'])

print('Cleaning UI...')
os.chdir(UI + "/docker")
subprocess.call(['python3', 'clean.py'])

# check and install weave or reset if already installed
weave = shutil.which('weave')

if weave == None:
    print('Installing weave...')
    subprocess.call(['sudo', 'curl', '-L', 'git.io/weave', '-o', '/usr/local/bin/weave'])
    subprocess.call(['sudo', 'chmod', '+x', '/usr/local/bin/weave'])
else:
    print('Reseting and stoping weave...')
    subprocess.call(['weave', 'reset'])
    subprocess.call(['weave', 'stop'])

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

    if ('rsommerard/wifidirect-master' in container_name) or ('rsommerard/wifidirect-node' in container_name) or ('rsommerard/wifidirect-ui' in container_name):
        subprocess.call(['docker', 'kill', container_id])
        subprocess.call(['docker', 'rm', '-f', container_id])
