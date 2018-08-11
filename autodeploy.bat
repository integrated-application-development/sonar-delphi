@echo off
REM This will compile, copy and restart the server automatically, change hardcoded paths as appropriate

echo Compiling (clean install)...
cmd /c "mvn clean install"

echo Copying compiled plugin to Sonar server install...

copy /b/v/y C:\Uni\StudioProject-FIT4002\FIT4002_SonarQube_Delphi\target\sonar-delphi-plugin-0.1.jar C:\sonarqube-7.2.1\extensions\plugins\sonar-delphi-plugin-0.1.jar /Y

echo Restarting server...

cmd /c "C:\sonarqube-7.2.1\bin\windows-x86-64\StopNTService.bat"
cmd /c "C:\sonarqube-7.2.1\bin\windows-x86-64\StartNTService.bat"