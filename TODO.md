 * Remove static method dependency. Use Dependency Injection. 
	* DelphiUtils 
 * Update plugin following the https://github.com/SonarSource/sonar-examples/blob/master/plugins/sonar-reference-plugin
 * Update antlr to version 4. See https://bitbucket.org/fabriciocolombo/delphiparser
 * Implements rules used by old delphi plugin. See elotech-pmd\src\main\resources\executorsets\delphiExecutorsBasic.xm
 * Define SQALE model to evaluate SQALE Rating and Technical Debt. See pmd example:
	https://github.com/SonarCommunity/sonar-pmd/blob/master/src/main/resources/com/sonar/sqale/pmd-model.xml
	https://github.com/SonarSource/sonar-findbugs/blob/master/src/main/resources/com/sonar/sqale/findbugs-model.xml
 * Support NUnit xml output format for unit tests (https://github.com/SonarCommunity/sonar-dotnet-tests-library), then we can user DUnitX as unit test tool.
 * http://docs.sonarqube.org/display/DEV/Internationalization
 * Excluded files and/or directories should be use org.sonar.api.scan.filesystem.FileExclusions api
 * Check http://docs.sonarqube.org/display/DEV/API+Changes and fix it
 * DeadCodeMetrics seems not be working for unused units
 * Metrics like DeadCode and Complexity should generates issues
 * Create integration tests like other plugins
 * Clean up dependencies to make plugin smaller
 * Update dependencies to latest version
 * Add support to create [custom rules](http://docs.sonarqube.org/display/DEV/Extending+Coding+Rules)
 * Make all test resources compilable on real delphi.
 
 * Find Contributors
   * http://stackoverflow.com/questions/29373268/which-version-of-sonarqube-for-depereciated-delphi-plugin
   * http://sonarqube-archive.15.x6.nabble.com/Delphi-Language-td5031312.html
   * http://sonarqube-archive.15.x6.nabble.com/sonar-dev-Sonar-delphi-plugin-no-longer-mantained-any-help-in-updating-it-td5029115.html
   
 * False positives
  * Do not use type aliases (Why it is evil?)
    * Class of class use case
		 TMyComponentClass = class of TComponent;
    * Defining array types
         TCustomArray = array of byte;
    * Defining derived type (Verify)
	     TMyInteger = type Integer;