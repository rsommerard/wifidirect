import os
import shutil
import subprocess

NODE = '/home/romain/Lab/WiFiDirect/System/Node'
DOCKER = NODE + '/docker'
TEMP = DOCKER + '/temp'
ZIP = NODE + '/target/universal/wifidirect-node-1.0.zip'

os.chdir(NODE)

if os.path.exists(TEMP):
    shutil.rmtree(TEMP)

os.mkdir(TEMP)

subprocess.call(['sbt', 'clean', 'universal:packageBin'])

shutil.copy(ZIP, TEMP)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-node', DOCKER])

subprocess.call(['docker', 'push', 'rsommerard/wifidirect-node'])


shutil.rmtree(TEMP)
subprocess.call(['sbt', 'clean'])
