#!/usr/bin/python3

import subprocess

print('Zipping logs...')
subprocess.call(['zip', 'log.zip', 'log/*'])
