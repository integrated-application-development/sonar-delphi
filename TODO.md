 * Update plugin following the https://github.com/SonarSource/sonar-examples/blob/master/plugins/sonar-reference-plugin
 * Update antlr to version 4. See https://bitbucket.org/fabriciocolombo/delphiparser
 * Implements rules used by old delphi plugin. See elotech-pmd\src\main\resources\executorsets\delphiExecutorsBasic.xm
 * Define SQALE model to evaluate SQALE Rating and Technical Debt. See pmd example: https://github.com/SonarCommunity/sonar-pmd/blob/master/src/main/resources/com/sonar/sqale/pmd-model.xml
 * Remove static method dependency: DelphiFile, DelphiProjectHelper, DelphiUtils. Use Dependency Injection.
 * Support NUnit xml output format for unit tests, then we can user DUnitX as unit test tool.