import os
import shutil
import subprocess
import sys

DOCKER = os.getcwd()
MASTER = str.join('/', DOCKER.split('/')[:-1])
ZIP = MASTER + '/target/universal/wifidirect-master-1.0.zip'

os.chdir(MASTER)
subprocess.call(['sbt', 'clean', 'universal:packageBin'])

if os.path.exists(DOCKER + '/wifidirect-master-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-master-1.0.zip')

shutil.copy(ZIP, DOCKER)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-master', DOCKER])
subprocess.call(['docker', 'push', 'rsommerard/wifidirect-master'])
