#!/usr/bin/python3

import subprocess

print('Sopping running containers...')
process = subprocess.Popen(['docker', 'ps', '-q'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
lines = output.strip().split('\n')
for l in lines:
    process = subprocess.Popen(['docker', 'kill', l], stdout=subprocess.PIPE)
    process.wait()

print('Done.')
