import os
import subprocess
import shutil
import time
import argparse

CWD = os.getcwd()
ROOT = str.join('/', CWD.split('/')[:-1])
MASTER = ROOT + '/system/Master'
NODE = ROOT + '/system/Node'
ANDROID = ROOT + '/android/GPSLocation'
APK = ANDROID + '/app/build/outputs/apk/app-debug.apk'
PACKAGE_ACTIVITY = 'fr.inria.rsommerard.gpslocation/.MainActivity'

parser = argparse.ArgumentParser(prog='wifidirect.py', description='WiFi-Direct Emulator')
parser.add_argument('-n', '--nb-emulators', type=int, default=2)
parser.add_argument('-bn', '--build-node', action='store_true')
parser.add_argument('-bm', '--build-master', action='store_true')
parser.add_argument('-bap', '--build-android-project', action='store_true')
parser.add_argument('-aprp', '--android-project-root-path', default=ANDROID, type=str)
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

    if ('rsommerard/wifidirect-master' in container_name) or ('rsommerard/wifidirect-node' in container_name):
        subprocess.call(['docker', 'kill', container_id])
        subprocess.call(['docker', 'rm', '-f', container_id])

# build the Android application
if args.build_android_project:
    print('Building the android appplication...')
    os.chdir(args.android_project_root_path)
    subprocess.call(['./gradlew', 'clean', 'assembleDebug'])

# build containers
if args.build_master:
    print('Building wifidirect-master...')
    os.chdir(MASTER + "/docker")
    subprocess.call(['python3', 'build.py'])

if args.build_node:
    print('Building wifidirect-node...')
    os.chdir(NODE + "/docker")
    subprocess.call(['python3', 'build.py', APK])

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

# launch nodes script
print('Launching node scripts...')
for i in range(0, args.nb_emulators):
    time.sleep(3)
    subprocess.Popen(['gnome-terminal', '--working-directory', CWD, '-e', 'python3 node.py ' + args.android_application_package_activity])
