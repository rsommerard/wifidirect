import os
import shutil
import subprocess

MASTER = '/home/romain/Lab/WiFiDirect/System/Master'
DOCKER = MASTER + '/docker'
TEMP = DOCKER + '/temp'
ZIP = MASTER + '/target/universal/wifidirect-master-1.0.zip'

os.chdir(MASTER)

if os.path.exists(TEMP):
    shutil.rmtree(TEMP)

os.mkdir(TEMP)

subprocess.call(['sbt', 'clean', 'universal:packageBin'])

shutil.copy(ZIP, TEMP)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-master', DOCKER])

subprocess.call(['docker', 'push', 'rsommerard/wifidirect-master'])


shutil.rmtree(TEMP)
subprocess.call(['sbt', 'clean'])
