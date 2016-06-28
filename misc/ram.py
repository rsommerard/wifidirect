#!/usr/bin/python3

import subprocess

process = subprocess.Popen(['free', '-m', '-g'], stdout=subprocess.PIPE)

output = str(process.communicate()[0], 'UTF-8')
free = output.split('\n')[1].split()[3]

print(free)
