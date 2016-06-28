#!/usr/bin/python3

import os
import shutil
import subprocess

CWD = os.getcwd()
AVD = os.path.expanduser('~/.android/avd')

# remove WiDi_One
print('Removing WiDi_One avd...')
if os.path.exists(AVD + '/WiDi_One.ini'):
    os.remove(AVD + '/WiDi_One.ini')

if os.path.exists(AVD + '/WiDi_One.avd'):
    shutil.rmtree(AVD + '/WiDi_One.avd')

# remove WiDi_Two
print('Removing WiDi_Two avd...')
if os.path.exists(AVD + '/WiDi_Two.ini'):
    os.remove(AVD + '/WiDi_Two.ini')

if os.path.exists(AVD + '/WiDi_Two.avd'):
    shutil.rmtree(AVD + '/WiDi_Two.avd')

# clean android projects
for folder in os.listdir("android"):
    os.chdir('android/' + folder)
    print('Cleaning ' + folder + ' project...')
    subprocess.call(['./gradlew', 'clean'])
    os.chdir(CWD)

# clean server
os.chdir(CWD + '/WiDiServer')
print('Cleaning WiDiServer project...')
subprocess.call(['sbt', 'clean'])
