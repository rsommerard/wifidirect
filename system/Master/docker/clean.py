import os
import subprocess

DOCKER = os.getcwd()
MASTER = str.join('/', DOCKER.split('/')[:-1])

os.chdir(MASTER)
subprocess.call(['sbt', 'clean'])

if os.path.exists(DOCKER + '/wifidirect-master-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-master-1.0.zip')

os.chdir(DOCKER)
filelist = [ f for f in os.listdir() if f.endswith(".txt") ]
for f in filelist:
    os.remove(DOCKER + '/' + f)
