#!/usr/bin/python3

import subprocess
import shutil
import os

AVD = os.path.expanduser('~/.android/avd')

process = subprocess.Popen(['android', 'list', 'avd'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if "WiDiOne" not in output:
    print('Installing WiDiOne avd...')
    process = subprocess.Popen(['android', 'create', 'avd', '-n', 'WiDiOne', '-t', 'android-19', '-b', 'google_apis/x86'], stdin=subprocess.PIPE)
    process.communicate(input='no'.encode())
    if os.path.exists(AVD + '/WiDiOne.ini'):
        os.remove(AVD + '/WiDiOne.ini')
    shutil.copy('avd/WiDiOne.ini', AVD)
    if os.path.exists(AVD + '/WiDiOne.avd/config.ini'):
        os.remove(AVD + '/WiDiOne.avd/config.ini')
    shutil.copy('avd/WiDiOne.avd/config.ini', AVD + '/WiDiOne.avd/config.ini')

if "WiDiTwo" not in output:
    print('Installing WiDiTwo avd...')
    process = subprocess.Popen(['android', 'create', 'avd', '-n', 'WiDiTwo', '-t', 'android-19', '-b', 'google_apis/x86'], stdin=subprocess.PIPE)
    process.communicate(input='no'.encode())
    if os.path.exists(AVD + '/WiDiTwo.ini'):
        os.remove(AVD + '/WiDiTwo.ini')
    shutil.copy('avd/WiDiTwo.ini', AVD)
    if os.path.exists(AVD + '/WiDiTwo.avd/config.ini'):
        os.remove(AVD + '/WiDiTwo.avd/config.ini')
    shutil.copy('avd/WiDiTwo.avd/config.ini', AVD + '/WiDiTwo.avd/config.ini')
