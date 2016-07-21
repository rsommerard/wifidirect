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
