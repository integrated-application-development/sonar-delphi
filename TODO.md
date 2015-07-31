 * Remove static method dependency. Use Dependency Injection. 
    * DelphiFile
	* DelphiUtils 
 * Update to Java 7 - http://docs.sonarqube.org/display/SONAR/Requirements
 * Update plugin following the https://github.com/SonarSource/sonar-examples/blob/master/plugins/sonar-reference-plugin
 * Update antlr to version 4. See https://bitbucket.org/fabriciocolombo/delphiparser
 * Implements rules used by old delphi plugin. See elotech-pmd\src\main\resources\executorsets\delphiExecutorsBasic.xm
 * Define SQALE model to evaluate SQALE Rating and Technical Debt. See pmd example:
	https://github.com/SonarCommunity/sonar-pmd/blob/master/src/main/resources/com/sonar/sqale/pmd-model.xml
	https://github.com/SonarSource/sonar-findbugs/blob/master/src/main/resources/com/sonar/sqale/findbugs-model.xml
 * Support NUnit xml output format for unit tests (https://github.com/SonarCommunity/sonar-dotnet-tests-library), then we can user DUnitX as unit test tool.
 * Replace Apache Commons Configuration (that is deprecated since release 2.12) by org.sonar.api.config.Settings
 * http://docs.sonarqube.org/display/DEV/Internationalization
 * Excluded files and/or directories should be use org.sonar.api.scan.filesystem.FileExclusions api
 * Check http://docs.sonarqube.org/display/DEV/API+Changes and fix it