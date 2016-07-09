import os
import subprocess

DOCKER = os.getcwd()
SERVICE_DISCOVERY = str.join('/', DOCKER.split('/')[:-1])

os.chdir(SERVICE_DISCOVERY)
subprocess.call(['sbt', 'clean', 'clean-files'])

if os.path.exists(DOCKER + '/wifidirect-servicediscovery-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-servicediscovery-1.0.zip')
