#!/bin/sh

# Relancement de Redis
redis-cli shutdown
redis-server conf/redis/redis.prod.conf &

# Sauvegarde des logs de l'ancienne application
mkdir ../loglog/"$(date +'%d-%m-%Y/%T')"/
sudo cp github_issues_dashboard-1.0-SNAPSHOT/logs/application.log ../loglog/"$(date +'%d-%m-%Y')"/

# Suppression du code compilé de l'ancienne application
sudo rm -rf github_issues_dashboard-1.0-SNAPSHOT/

# Restart la nouvelle application
./activator play-update-secret
./activator clean universal:package-zip-tarball
tar zxvf target/universal/*.tgz

cd github_issues_dashboard-1.0-SNAPSHOT/

sudo ./bin/github_issues_dashboard -mem 8192 -J-server -J-javaagent:/home/Jules/newrelic/newrelic.jar -DapplyEvolutions.default=true -Dhttp.port=80 &
