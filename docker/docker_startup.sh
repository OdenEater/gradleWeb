#!/bin/bash
#docker-compose.ymlを指定してDBコンテナを起動

# This script starts the Docker containers defined in docker-compose.yml.
# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if docker compose is installed
if ! command -v docker compose &> /dev/null; then
    echo "docker compose is not installed. Please install docker compose first."
    exit 1
fi

# 実行ディレクトリをスクリプトのある場所に移動
cd $(dirname "$0")

# Start the Docker containers using docker-compose
docker compose -f docker-compose.yml up -d
if [ $? -ne 0 ]; then
    echo "Failed to start Docker containers. Please check the docker-compose file."
    exit 1
fi

# Wait for a 10 seconds to ensure the containers are up
sleep 10

echo "Docker containers started successfully."