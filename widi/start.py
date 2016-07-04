#!/usr/bin/python3

import subprocess
import sys
import time

process = subprocess.Popen(['adb', 'devices'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if 'emulator-5554' in output:
    print('Please close all running emulator')
    sys.exit(0)

if 'emulator-5556' in output:
    print('Please close all running emulator')
    sys.exit(0)

process = subprocess.Popen(['android', 'list', 'avd'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

if 'WiDiOne' not in output:
    print('WiDiOne is not installed')
    sys.exit(0)

if 'WiDiTwo' not in output:
    print('WiDiTwo is not installed')
    sys.exit(0)

print('Starting WiDiOne...')
subprocess.Popen(['emulator', '-avd', 'WiDiOne', '-port', '5554'])

# check if WiDiOne is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

time.sleep(3)

print('Starting WiDiTwo...')
subprocess.Popen(['emulator', '-avd', 'WiDiTwo', '-port', '5556'])

# check if WiDiTwo is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')
