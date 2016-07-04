#!/usr/bin/python3

import os
import shutil
import subprocess

CWD = os.getcwd()
AVD = os.path.expanduser('~/.android/avd')

# remove WiDiOne
print('Removing WiDiOne avd...')
if os.path.exists(AVD + '/WiDiOne.ini'):
    os.remove(AVD + '/WiDiOne.ini')

if os.path.exists(AVD + '/WiDiOne.avd'):
    shutil.rmtree(AVD + '/WiDiOne.avd')

# remove WiDiTwo
print('Removing WiDiTwo avd...')
if os.path.exists(AVD + '/WiDiTwo.ini'):
    os.remove(AVD + '/WiDiTwo.ini')

if os.path.exists(AVD + '/WiDiTwo.avd'):
    shutil.rmtree(AVD + '/WiDiTwo.avd')

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
