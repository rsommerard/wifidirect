#!/usr/bin/python3

import subprocess
import shutil
import os

AVD = os.path.expanduser('~/.android/avd')

process = subprocess.Popen(['android', 'list', 'avd'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if "WiDi_One" not in output:
    print('Installing WiDi_One avd...')
    shutil.copy('avd/WiDi_One.ini', AVD)
    shutil.copytree('avd/WiDi_One.avd', AVD + '/WiDi_One.avd')

if "WiDi_Two" not in output:
    print('Installing WiDi_Two avd...')
    shutil.copy('avd/WiDi_Two.ini', AVD)
    shutil.copytree('avd/WiDi_Two.avd', AVD + '/WiDi_Two.avd')

