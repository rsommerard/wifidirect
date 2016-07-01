#!/usr/bin/python3

import subprocess
import os
import argparse
import time

CWD = os.getcwd()
SERVER = CWD + '/WiDiServer'
PATH = CWD + '/android/WiDiTestingProject'
PACKAGE_ACTIVITY = 'fr.inria.rsommerard.widitestingproject/.MainActivity'
FILE = 'widi/src/main/java/fr/inria/rsommerard/widi/core/WiDi.java'

parser = argparse.ArgumentParser(prog='start.py', description='Start two emulator to test the WiDi implementation')
parser.add_argument('-p', '--path', default=PATH, type=str)
parser.add_argument('-aapa', '--android_application_package_activity', default=PACKAGE_ACTIVITY, type=str)
args = parser.parse_args()

def writeWiDiFile(serverPort, inPort, outPort):
    os.chdir(args.path)
    if os.path.exists(FILE):
        os.remove(FILE)
    with open(FILE, "w+") as f:
        f.write("package fr.inria.rsommerard.widi.core;\n")
        f.write("\n")
        f.write("public abstract class WiDi {\n")
        f.write("\n")
        f.write("    public static final String TAG = \"WiDi\";\n")
        f.write("\n")
        f.write("    public static final String SERVER_ADDRESS = \"10.0.2.2\";\n")
        f.write("    public static final int SERVER_PORT = " + str(serverPort) + ";\n")
        f.write("\n")
        f.write("    public static final int SOCKET_TIMEOUT = 1000;\n")
        f.write("\n")
        f.write("    public static final int DATA_EXCHANGE_PORT_OUT = " + str(outPort) + ";\n")
        f.write("    public static final int DATA_EXCHANGE_PORT_IN = " + str(inPort) + ";\n")
        f.write("}\n")

# build the testing server
os.chdir(SERVER)
process = subprocess.Popen(['sbt', 'clean', 'universal:packageBin'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
os.chdir(SERVER + '/target/universal')
subprocess.call(['unzip', 'widiserver-1.0.zip'])
    
# check if WiDi_One is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5554', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

# check if WiDi_Two is UP
process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while '1' not in output:
    time.sleep(3)
    process = subprocess.Popen(['adb', '-s', 'emulator-5556', 'shell', 'getprop', 'sys.boot_completed'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

# launch the testing server
subprocess.Popen(['gnome-terminal', '-e', SERVER + '/target/universal/widiserver-1.0/bin/widiserver'])

# build the android application for WiDi_One
print('Building the android appplication for WiDi_One...')
writeWiDiFile(54412, 11131, 11113)
subprocess.call(['./gradlew', 'clean', 'assembleDebug'])

# install the application on WiDi_One
print('Uninstalling the old application on WiDi_One...')
subprocess.call(['adb', '-s', 'emulator-5554', 'uninstall', PACKAGE_ACTIVITY.split('/')[0]], stdout=subprocess.PIPE)
print('Installing the application on WiDi_One...')
subprocess.call(['adb', '-s', 'emulator-5554', 'install', '-r', args.path + '/app/build/outputs/apk/app-debug.apk'])

# build the android application for WiDi_Two
print('Building the android appplication for WiDi_Two...')
writeWiDiFile(54421, 11113, 11131)
subprocess.call(['./gradlew', 'clean', 'assembleDebug'])

# install the application on WiDi_Two
print('Uninstalling the old application on WiDi_Two...')
subprocess.call(['adb', '-s', 'emulator-5556', 'uninstall', PACKAGE_ACTIVITY.split('/')[0]], stdout=subprocess.PIPE)
print('Installing the application on WiDi_Two...')
subprocess.call(['adb', '-s', 'emulator-5556', 'install', '-r', args.path + '/app/build/outputs/apk/app-debug.apk'])

subprocess.Popen(['gnome-terminal', '-e', 'adb -s emulator-5554 logcat'])
subprocess.Popen(['gnome-terminal', '-e', 'adb -s emulator-5556 logcat'])

# launch the application on WiDi_One
print('Launching the application on WiDi_One...')
subprocess.call(['adb', '-s', 'emulator-5554', 'shell', 'am', 'start', '-n', PACKAGE_ACTIVITY])

# launch the application on WiDi_Two
print('Launching the application on WiDi_Two...')
subprocess.call(['adb', '-s', 'emulator-5556', 'shell', 'am', 'start', '-n', PACKAGE_ACTIVITY])

