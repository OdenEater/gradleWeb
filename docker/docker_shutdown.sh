#!/bin/bash
#docker composeコマンドでDockerをすべてシャットダウン

# This script stops and removes all Docker containers, networks, images, and volumes.
# Ensure the script is run with root privileges
docker compose down