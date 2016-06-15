import os
import shutil
import subprocess
import sys

APK = sys.argv[1]
DOCKER = os.getcwd()
NODE = str.join('/', DOCKER.split('/')[:-1])
ZIP = NODE + '/target/universal/wifidirect-node-1.0.zip'

if os.path.exists(DOCKER + '/app-debug.apk'):
    os.remove(DOCKER + '/app-debug.apk')

shutil.copy(APK, DOCKER)

os.chdir(NODE)
subprocess.call(['sbt', 'clean', 'universal:packageBin'])

if os.path.exists(DOCKER + '/wifidirect-node-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-node-1.0.zip')

shutil.copy(ZIP, DOCKER)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-node', DOCKER])
subprocess.call(['docker', 'push', 'rsommerard/wifidirect-node'])
