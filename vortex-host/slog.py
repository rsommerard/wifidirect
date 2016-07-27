#!/usr/bin/python3

import os
import sys
import subprocess

print('Reading containers.info file....')
if not os.path.exists('containers.info'):
    print('The containers.info file does not exist.')
    sys.exit(1)

if not os.path.exists('log'):
    print('The log folder does not exist.')
    sys.exit(1)

print('Adding Master in containers.info file.')
process = subprocess.Popen(['docker', 'ps', '-a'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if 'rsommerard/wifidirect-master' in output:
    entries = output.strip().split('\n')
    for entry in entries:
        if 'rsommerard/wifidirect-master' in entry:
            with open('containers.info', 'a+') as f:
                f.write("Master=" + entry.split()[0])
            break

with open('containers.info', 'r') as f:
    content = f.read().strip()

csplit = content.split('\n')

for c in csplit:
    name, id = c.split('=')
    print('Processing ' + name + ' logs...')
    process = subprocess.Popen(['docker', 'logs', id], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

    with open('log/' + name + '_' + id + '.log', 'w+') as f:
        f.write(output)

print('Done.')
