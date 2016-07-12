#!/usr/bin/python3

import subprocess
import os

CWD = os.getcwd()

GPS_LOCATION = CWD + '/android/GPSLocation'
VIEWER = CWD + '/viewer'
MASTER = CWD + '/system/Master/docker'
NODE = CWD + '/system/Node/docker'
UI = CWD + '/system/UI/docker'
SERVICE_DISCOVERY = CWD + '/system/ServiceDiscovery/docker'
WIDI = CWD + '/widi'

os.chdir(GPS_LOCATION)
subprocess.call(['python3', 'clean.py'])

os.chdir(VIEWER)
subprocess.call(['python3', 'clean.py'])

os.chdir(MASTER)
subprocess.call(['python3', 'clean.py'])

os.chdir(NODE)
subprocess.call(['python3', 'clean.py'])

os.chdir(UI)
subprocess.call(['python3', 'clean.py'])

os.chdir(SERVICE_DISCOVERY)
subprocess.call(['python3', 'clean.py'])

os.chdir(WIDI)
subprocess.call(['python3', 'clean.py'])
