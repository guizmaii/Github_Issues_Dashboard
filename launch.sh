#!/bin/sh

rm -rf github_issues_dashboard-1.0-SNAPSHOT/

./activator play-update-secret
./activator clean universal:package-zip-tarball
tar zxvf target/universal/*.tgz

./github_issues_dashboard-1.0-SNAPSHOT/bin/github_issues_dashboard -J-javaagent:/home/Jules/newrelic/newrelic.jar
