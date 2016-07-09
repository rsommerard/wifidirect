import os
import shutil
import subprocess
import argparse
import json

parser = argparse.ArgumentParser(prog='build.py', description='Docker ServiceDiscovery container builder')
parser.add_argument('-p', '--push', action='store_true')
args = parser.parse_args()

DOCKER = os.getcwd()
SERVICE_DISCOVERY = str.join('/', DOCKER.split('/')[:-1])
ZIP = SERVICE_DISCOVERY + '/target/universal/wifidirect-servicediscovery-1.0.zip'

os.chdir(SERVICE_DISCOVERY)
subprocess.call(['sbt', 'clean', 'universal:packageBin'])

if os.path.exists(DOCKER + '/wifidirect-servicediscovery-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-servicediscovery-1.0.zip')

shutil.copy(ZIP, DOCKER)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-servicediscovery', DOCKER])

if args.push:
    credentials = os.path.expanduser('~/.docker/config.json')

    if os.path.exists(credentials):
        with open(credentials) as jsf:
            data = json.load(jsf)
            if len(data['auths']) == 0:
                subprocess.call(['docker', 'login'])

    subprocess.call(['docker', 'push', 'rsommerard/wifidirect-servicediscovery'])
