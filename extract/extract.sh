#!/bin/sh

#set -x
set -e

if test ! -d factorio-data; then
	git clone https://github.com/wube/factorio-data.git
fi
cd factorio-data
git pull
cd ..

docker-compose build --force-rm --pull
docker-compose run --rm -u `id -u`:`id -g` -v `realpath factorio-data`:/var/factorio-data -v `realpath ../resources`:/var/extract-result extract
