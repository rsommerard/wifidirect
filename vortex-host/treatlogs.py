#!/usr/bin/python3

import os
import sys
import shutil

if not os.path.exists('log'):
    print('log folder does not exist.')
    sys.exit(1)

logs = [ f for f in os.listdir('log') if f.endswith('.log') ]

for lf in logs:
    print(lf)
    if lf.startswith('Node'):
        with open('log/' + lf, 'r') as f:
            for line in f:
                if line.startswith('IP'):
                    ip = line.split()[1]
                    break

        name = 'N' + ip.replace('.', '')
        shutil.move('log/' + lf, 'log/' + name)
