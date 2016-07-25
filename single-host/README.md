## Launcher


### wifidirect.py

Launches the emulation architecture.

Usage: `./wifidirect.py [-n <number>] [-ball] [-bm] [-bn] [-bui] [-bsd] [-bap] [-p]`

- `-n|--nb-emulators: how many nodes to launch. Default: 2`
- `-ball|--build-all: build all containers. Default: not enabled`
- `-bm|--build-master: build the master container. Default: not enabled`
- `-bn|--build-node: build the node container. Default: not enabled`
- `-bui|--build-ui: build the ui container. Default: not enabled`
- `-bsd|--build-servicediscovery: build the servicediscovery container. Default: not enabled`
- `-bap|--build-android-project: build the android-project which is used in the node container. Default: not enabled`
- `-p|--push: push containers to dockerhub. Default: not enabled`


### master.py, node.py, ui.py and servicediscovery.py

Specific launching scripts for each container.



## Tools


### dclean-hard.py

Removes the info file and the log folder
It also kills all running containers and deletes stopped containers.

Usage: `./dclean-hard.py`


### dclean-soft.py

Removes the info file and the log folder
It also kills only own running containers and deletes own stopped containers (rsommerard and weaveworks tagged).

Usage: `./dclean-soft.py`


### dstop.py

Kills all running containers.

Usage: `./dstop.py`


### slog.py

Generates logs of running or stopped containers.

Usage: `./slog.py`
