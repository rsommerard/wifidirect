#!/usr/bin/python3

import os
import subprocess
import shutil
import time
import argparse

CWD = os.getcwd()
ROOT = str.join('/', CWD.split('/')[:-1])
MASTER = ROOT + '/system/Master'
NODE = ROOT + '/system/Node'
UI = ROOT + '/system/UI'
SERVICE_DISCOVERY = ROOT + '/system/ServiceDiscovery'
# ANDROID = ROOT + '/android/GPSLocation'
ANDROID = ROOT + '/android/WiDiTestingProject/'
VIEWER = ROOT + '/viewer'
APK = ANDROID + '/app/build/outputs/apk/app-debug.apk'
# PACKAGE_ACTIVITY = 'fr.inria.rsommerard.gpslocation/.MainActivity'
PACKAGE_ACTIVITY = 'fr.inria.rsommerard.widitestingproject/.MainActivity'

parser = argparse.ArgumentParser(prog='wifidirect.py', description='WiFi-Direct Emulator')
parser.add_argument('-n', '--nb-emulators', type=int, default=2)
parser.add_argument('-bn', '--build-node', action='store_true')
parser.add_argument('-ball', '--build-all', action='store_true')
parser.add_argument('-bui', '--build-ui', action='store_true')
parser.add_argument('-bsd', '--build-servicediscovery', action='store_true')
parser.add_argument('-vp', '--viewer_path', default=VIEWER, type=str)
parser.add_argument('-bm', '--build-master', action='store_true')
parser.add_argument('-bap', '--build-android-project', action='store_true')
parser.add_argument('-aprp', '--android-project-root-path', default=ANDROID, type=str)
parser.add_argument('-p', '--push', action='store_true')
parser.add_argument('-aapa', '--android_application_package_activity', default=PACKAGE_ACTIVITY, type=str)
args = parser.parse_args()

# check and install weave or reset if already installed
weave = shutil.which('weave')

if weave == None:
    print('Installing weave...')
    subprocess.call(['sudo', 'curl', '-L', 'git.io/weave', '-o', '/usr/local/bin/weave'])
    subprocess.call(['sudo', 'chmod', '+x', '/usr/local/bin/weave'])
else:
    print('Reseting weave...')
    subprocess.call(['weave', 'reset'])

# remove old containers
process = subprocess.Popen(['docker', 'ps', '-a'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
cont = output.split('\n')[1:]

for c in cont:
    if not c:
        continue

        print('Removing ' + container_name + '...')

    container_id = c.split()[0]
    container_name = c.split()[1]

    if ('rsommerard/wifidirect-master' in container_name) or ('rsommerard/wifidirect-node' in container_name) or ('rsommerard/wifidirect-servicediscovery' in container_name):
        subprocess.call(['docker', 'kill', container_id])
        subprocess.call(['docker', 'rm', '-f', container_id])

# build the Android application
if args.build_android_project or args.build_all:
    print('Building the android appplication...')
    os.chdir(args.android_project_root_path)
    subprocess.call(['./gradlew', 'clean', 'assembleDebug'])

# build containers
if args.build_master or args.build_all:
    print('Building wifidirect-master...')
    os.chdir(MASTER + "/docker")

    cmd = ['python3', 'build.py']
    if args.push:
        cmd.append('-p')

    subprocess.call(cmd)

if args.build_node or args.build_all:
    print('Building wifidirect-node...')
    os.chdir(NODE + "/docker")

    cmd = ['python3', 'build.py', '-apk', APK]
    if args.push:
        cmd.append('-p')

    subprocess.call(cmd)

if args.build_servicediscovery or args.build_all:
    print('Building wifidirect-servicediscovery...')
    os.chdir(SERVICE_DISCOVERY + "/docker")

    cmd = ['python3', 'build.py']
    if args.push:
        cmd.append('-p')

    subprocess.call(cmd)

if args.build_ui or args.build_all:
    print('Building wifidirect-ui...')
    os.chdir(UI + "/docker")

    cmd = ['python3', 'build.py']
    if args.push:
        cmd.append('-p')

    subprocess.call(cmd)


os.chdir(CWD)

# launch master script
print('Launching master script...')
subprocess.Popen(['gnome-terminal', '--working-directory', CWD, '-e', 'python3 master.py ' + str(args.nb_emulators)])

# waiting master container
print('Waiting master container...')
process = subprocess.Popen(['docker', 'ps'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')

while 'rsommerard/wifidirect-master' not in output:
    time.sleep(3)
    process = subprocess.Popen(['docker', 'ps'], stdout=subprocess.PIPE)
    output = str(process.communicate()[0], 'UTF-8')

# launch ui script
if args.viewer_path:
    print('Launching ui scripts...')
    subprocess.Popen(['gnome-terminal', '--working-directory', CWD, '-e', 'python3 ui.py ' + args.viewer_path])

# launch service discovery script
print('Launching servicediscovery script...')
subprocess.Popen(['gnome-terminal', '--working-directory', CWD, '-e', 'python3 servicediscovery.py '])

# launch nodes script
print('Launching node scripts...')
for i in range(0, args.nb_emulators):
    time.sleep(3)
    subprocess.Popen(['gnome-terminal', '--working-directory', CWD, '-e', 'python3 node.py ' + args.android_application_package_activity])
