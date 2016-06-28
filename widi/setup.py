#!/usr/bin/python3

import subprocess
import shutil
import os

AVD = os.path.expanduser('~/.android/avd')

process = subprocess.Popen(['android', 'list', 'avd'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if "WiDi_One" not in output:
    print('Installing WiDi_One avd...')
    process = subprocess.Popen(['android', 'create', 'avd', '-n', 'WiDi_One', '-t', 'android-22', '-b', 'google_apis/x86'], stdin=subprocess.PIPE)
    process.communicate(input='no'.encode())
    if os.path.exists(AVD + '/WiDi_One.ini'):
        os.remove(AVD + '/WiDi_One.ini')
    shutil.copy('avd/WiDi_One.ini', AVD)
    if os.path.exists(AVD + '/WiDi_One.avd/config.ini'):
        os.remove(AVD + '/WiDi_One.avd/config.ini')
    shutil.copy('avd/WiDi_One.avd/config.ini', AVD + '/WiDi_One.avd/config.ini')

if "WiDi_Two" not in output:
    print('Installing WiDi_Two avd...')
    process = subprocess.Popen(['android', 'create', 'avd', '-n', 'WiDi_Two', '-t', 'android-22', '-b', 'google_apis/x86'], stdin=subprocess.PIPE)
    process.communicate(input='no'.encode())
    if os.path.exists(AVD + '/WiDi_Two.ini'):
        os.remove(AVD + '/WiDi_Two.ini')
    shutil.copy('avd/WiDi_Two.ini', AVD)
    if os.path.exists(AVD + '/WiDi_Two.avd/config.ini'):
        os.remove(AVD + '/WiDi_Two.avd/config.ini')
    shutil.copy('avd/WiDi_Two.avd/config.ini', AVD + '/WiDi_Two.avd/config.ini')

