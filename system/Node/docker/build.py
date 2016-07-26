import os
import shutil
import subprocess
import argparse
import json

parser = argparse.ArgumentParser(prog='build.py', description='Docker Node container builder')
parser.add_argument('-apk', '--apk-path', type=str)
parser.add_argument('-p', '--push', action='store_true')
args = parser.parse_args()

APK = args.apk_path
DOCKER = os.getcwd()
NODE = str.join('/', DOCKER.split('/')[:-1])
ZIP = NODE + '/target/universal/wifidirect-node-1.0.zip'

if os.path.exists(DOCKER + '/app-debug.apk'):
    os.remove(DOCKER + '/app-debug.apk')

os.chdir(NODE)
subprocess.call(['sbt', 'clean', 'universal:packageBin'])

if os.path.exists(DOCKER + '/wifidirect-node-1.0.zip'):
    os.remove(DOCKER + '/wifidirect-node-1.0.zip')

shutil.copy(ZIP, DOCKER)

subprocess.call(['docker', 'build', '-t', 'rsommerard/wifidirect-node', DOCKER])

if args.push:
    credentials = os.path.expanduser('~/.docker/config.json')

    if os.path.exists(credentials):
        with open(credentials) as jsf:
            data = json.load(jsf)
            if len(data['auths']) == 0:
                subprocess.call(['docker', 'login'])

    subprocess.call(['docker', 'push', 'rsommerard/wifidirect-node'])
