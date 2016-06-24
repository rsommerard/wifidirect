import subprocess
import os
import sys
import time

print('UI')

viewer_path = sys.argv[1]

# set weave env before launching containers
print("Setting weave env...")
process = subprocess.Popen(['weave', 'env'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
env = output.split(' ')[1:]
for e in env:
    name, value = e.split('=')
    if not value.isspace():
        os.environ[name] = value

# start ui container
print("Launching wifidirect-ui container...")
ui_container = subprocess.Popen(['docker', 'run', '--rm', '-it', 'rsommerard/wifidirect-ui'])

time.sleep(3)

process = subprocess.Popen(['docker', 'ps'], stdout=subprocess.PIPE)
output = str(process.communicate()[0], 'UTF-8')
containers = output.split('\n')[1:]

container_ip = '172.17.0.2'
for container in containers:
    if "rsommerard/wifidirect-ui" in container:
        container_id = container.split(' ')[0]
        process = subprocess.Popen(['docker', 'inspect', '--format', '{{ .NetworkSettings.IPAddress }}', container_id], stdout=subprocess.PIPE)
        container_ip = str(process.communicate()[0], 'UTF-8').strip()

print("Container IP: " + container_ip)

os.chdir(viewer_path)

if os.path.exists('config.js'):
    os.remove('config.js')

with open('config.js', "w+") as cf:
    cf.write("var config = {};\n")
    cf.write("config.url = \"http://" + container_ip + ":8080\";\n")
    cf.write("module.exports = config;\n")

ui_container.wait()
