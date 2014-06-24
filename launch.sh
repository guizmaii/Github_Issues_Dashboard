#!/bin/sh

cat /var/run/redis.pid | kill
redis-server conf/redis/redis.prod.conf &

rm -rf github_issues_dashboard-1.0-SNAPSHOT/

./activator play-update-secret
./activator clean universal:package-zip-tarball
tar zxvf target/universal/*.tgz

cd github_issues_dashboard-1.0-SNAPSHOT/

sudo ./bin/github_issues_dashboard -mem 8192 -J-server -J-javaagent:/home/Jules/newrelic/newrelic.jar -DapplyEvolutions.default=true -Dhttp.port=80
