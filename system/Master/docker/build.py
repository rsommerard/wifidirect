import os
import shutil
import subprocess
import argparse
import json

parser = argparse.ArgumentParser(prog='build.py', description='Docker Master container builder')
parser.add_argument('-p', '--push', action='store_true')
args = parser.parse_args()

DOCKER = os.getcwd()
MASTER = str.join('/', DOCKER.split('/')[:-1])
ZIP = MASTER + '/target/universal/wifidirect-master-1.0.zip'

os.chdir(MASTER)
subprocess.call(['sbt', 'clean', 'universal:packageBin'])

if os.path.exists(DOCKER + '/wifidirect-master-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-master-1.0.zip')

shutil.copy(ZIP, DOCKER)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-master', DOCKER])

if args.push:
    credentials = os.path.expanduser('~/.docker/config.json')

    if os.path.exists(credentials):
        with open(credentials) as jsf:
            data = json.load(jsf)
            if len(data['auths']) == 0:
                subprocess.call(['docker', 'login'])

    subprocess.call(['docker', 'push', 'rsommerard/wifidirect-master'])
