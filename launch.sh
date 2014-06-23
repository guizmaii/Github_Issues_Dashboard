#!/bin/sh

rm -rf github_issues_dashboard-1.0-SNAPSHOT/

cd ..

Github_Issues_Dashboard/activator play-update-secret
Github_Issues_Dashboard/activator clean universal:package-zip-tarball
tar zxvf Github_Issues_Dashboard/target/universal/*.tgz

./Github_Issues_Dashboard/github_issues_dashboard-1.0-SNAPSHOT/bin/github_issues_dashboard -J-javaagent:newrelic/newrelic.jar
