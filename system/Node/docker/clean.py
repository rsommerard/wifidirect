import os
import subprocess

DOCKER = os.getcwd()
NODE = str.join('/', DOCKER.split('/')[:-1])

os.chdir(NODE)
subprocess.call(['sbt', 'clean', 'clean-files'])

if os.path.exists(DOCKER + '/wifidirect-node-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-node-1.0.zip')

os.chdir(DOCKER)
filelist = [ f for f in os.listdir() if f.endswith(".apk") ]
for f in filelist:
    os.remove(DOCKER + '/' + f)
