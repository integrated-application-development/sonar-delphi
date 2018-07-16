#!/bin/bash

# for unix systems

echo "building project"
mvn clean install

echo "moving compiled .jar file to sonarqube extensions"
cp target/sonar-delphi-plugin-0.3.4.jar <YOUR PATH TO SONARQUBE>/sonarqube-7.2.1/extensions/plugins

echo "starting up sonarqube"
<YOUR PATH TO SONARQUBE>/sonarqube-7.2.1/bin/<YOUR OS>/sonar.sh console
