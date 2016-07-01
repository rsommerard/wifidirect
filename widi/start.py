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

if 'WiDi_One' not in output:
    print('WiDi_One is not installed')
    sys.exit(0)

if 'WiDi_Two' not in output:
    print('WiDi_Two is not installed')
    sys.exit(0)

print('Starting WiDi_One...')
subprocess.Popen(['emulator', '-avd', 'WiDi_One', '-port', '5554'])

# check if WiDi_One is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

time.sleep(3)
    
print('Starting WiDi_Two...')
subprocess.Popen(['emulator', '-avd', 'WiDi_Two', '-port', '5556'])

# check if WiDi_Two is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

