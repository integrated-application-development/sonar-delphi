#!/bin/bash

# for unix systems

echo "building project"
mvn clean install

echo "moving compiled .jar file to sonarqube extensions"
cp target/sonar-delphi-plugin-0.1.jar ~/Documents/Monash/FIT4002/sonarqube-7.2/extensions/plugins

echo "starting up sonarqube"
~/Documents/Monash/FIT4002/sonarqube-7.2/bin/macosx-universal-64/sonar.sh console

#sleep 30

#cd ~/Documents/Monash/FIT4002/TheAgileExcuse/FIT4002_SonarQube_Delphi/src/main/delphi/
#~/Documents/Monash/FIT4002/sonar-scanner-3.2.0.1227-macosx/bin/./sonar-scanner