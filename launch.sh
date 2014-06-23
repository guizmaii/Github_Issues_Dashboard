#!/bin/sh

rm -rf github_issues_dashboard-1.0-SNAPSHOT/

activator play-update-secret && activator clean dist && unzip target/universal/*.zip

./github_issues_dashboard-1.0-SNAPSHOT/bin/github_issues_dashboard -J-javaagent:../newrelic/newrelic.jar
