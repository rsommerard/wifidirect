#!/usr/bin/python3

import os
import subprocess
import shutil

print('Cleaning info file and log folder....')
if os.path.exists('log'):
    shutil.rmtree('log')

filelist = [ f for f in os.listdir() if f.endswith('.log') or f.endswith('.info') ]
for f in filelist:
    os.remove(f)

# TODO: make a selective clean
# print('Sopping running containers...')
# process = subprocess.Popen(['docker', 'ps', '-q'], stdout=subprocess.PIPE)
# output = str(process.communicate()[0], 'UTF-8')
# lines = output.strip().split('\n')
# for l in lines:
#     process = subprocess.Popen(['docker', 'kill', l], stdout=subprocess.PIPE)
#     process.wait()
#
# print('Removing stopped containers...')
# process = subprocess.Popen(['docker', 'ps', '-a', '-q'], stdout=subprocess.PIPE)
# output = str(process.communicate()[0], 'UTF-8')
# lines = output.strip().split('\n')
# for l in lines:
#     process = subprocess.Popen(['docker', 'rm', l], stdout=subprocess.PIPE)
#     process.wait()

print('Done.')
