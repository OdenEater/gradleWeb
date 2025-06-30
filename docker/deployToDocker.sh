#!/bin/bash

# This script runs docker and deploys a WAR file to a Tomcat container.
# Ensure the script is run with root privileges

#OSがubuntuかそれ以外か判定
if [ -f /etc/lsb-release ]; then
    OS="ubuntu"
else
    OS="other"
fi

# Install Docker
if [ "$OS" = "ubuntu" ]; then
    apt-get update -y
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
    curl -fsSL https://download.docker.com/linux/$(lsb_release -is | tr '[:upper:]' '[:lower:]')/gpg | apt-key add -
    echo "deb [arch=$(dpkg --print-architecture)] https://download.docker.com/linux/$(lsb_release -is | tr '[:upper:]' '[:lower:]') $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io
else
    dnf update -y
    dnf install -y dnf-plugins-core
    dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    dnf install -y docker-ce docker-ce-cli containerd.io
fi

# Start Docker service
systemctl start docker

#Dockerのバージョン確認。コマンドに失敗したら詳細なログを排出。
docker --version || { echo "Docker installation failed"; exit 1; }

