/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.codecoverage.aqtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DatabaseConnectionProperties;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.HSQLServerUtil;

public class AQTimeCoverageParserTest extends DBTestCase {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private static final String DB_FILE = "/org/sonar/plugins/delphi/AQTimeDB/db.xml";
  private AQTimeCoverageParser parser;
  private Connection connection;
  private Map<String, String> connectionProps;

  public AQTimeCoverageParserTest() {
    System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:hsqldb:mem:CC");
    System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa");
    System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "");
  }

  @Override
  public void setUp() throws Exception {

    HSQLServerUtil.getInstance().start("CC", 7654);

    connectionProps = new HashMap<String, String>();
    connectionProps.put(DelphiPlugin.JDBC_USER_KEY, "sa");
    connectionProps.put(DelphiPlugin.JDBC_PASSWORD_KEY, "");
    connectionProps.put(DelphiPlugin.JDBC_DRIVER_KEY, "org.hsqldb.jdbcDriver");
    connectionProps.put(DelphiPlugin.JDBC_URL_KEY, "jdbc:hsqldb:mem:CC");
    Class.forName("org.hsqldb.jdbcDriver");
    connection = DriverManager.getConnection("jdbc:hsqldb:mem:CC", getDatabaseProperties());

    // create tables for DB
    Statement stmt = connection.createStatement();
    StringBuilder query = new StringBuilder();
    query
        .append("CREATE TABLE LIGHT_COVERAGE_PROFILER_LINES ( INST_ID INT, ID INT, REC_ID INT, PARENT_ID INT, COL_MARK INT, COL_SOURCE_LINE INT, PRIMARY KEY(ID) );");
    query
        .append("CREATE TABLE LIGHT_COVERAGE_PROFILER_SOURCE_FILES_DATA ( INST_ID INT, ID INT, REC_ID INT, PARENT_ID INT, COL_MARK INT, COL_FILE_NAME VARCHAR(255), COL_CALCULATED INT, COL_SKIP_COUNT INT, COL____COVERED FLOAT, PRIMARY KEY(ID) );");
    stmt.executeQuery(query.toString());
    stmt.close();
    super.setUp();
    DatabaseConnectionProperties properties = mock(DatabaseConnectionProperties.class);
    when(properties.getJDBCProperties()).thenReturn(connectionProps);

    parser = new AQTimeCoverageParser();
    parser.setConnectionProperties(properties);
    parser.setPrefix("");
  }

  private Properties getDatabaseProperties() {
    Properties properties = new Properties();
    for (String key : connectionProps.keySet()) {
      properties.put(key, connectionProps.get(key));
    }
    return properties;
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    HSQLServerUtil.getInstance().stop();
  }

  public void testParse() {

    Project project = mock(Project.class);
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

    File baseDir = DelphiUtils.getResource(FILE_NAME);
    when(project.getFileSystem()).thenReturn(pfs);
    when(pfs.getBasedir()).thenReturn(baseDir);

    List<File> sourceFiles = DelphiUtils.getSourceFiles(project);
    List<File> sourceDirs = DelphiUtils.getSourceDirectories(project);
    when(pfs.getSourceDirs()).thenReturn(sourceDirs);

    List<InputFile> inputSourceFiles = new ArrayList<InputFile>();
    for (File srcFile : sourceFiles) {
      InputFile iFile = mock(InputFile.class);
      when(iFile.getFile()).thenReturn(srcFile);
      inputSourceFiles.add(iFile);
    }

    when(pfs.mainFiles(DelphiLanguage.KEY)).thenReturn(inputSourceFiles);

    DebugSensorContext context = new DebugSensorContext();
    parser.setSourceFiles(inputSourceFiles);
    parser.setSourceDirectories(sourceDirs);
    parser.parse(project, context);

    String coverage_names[] = { "Globals.pas:coverage", "MainWindow.pas:coverage" };
    double coverage_values[] = { 100.00, 50.00 };
    String lineHits_names[] = { "Globals.pas:coverage_line_hits_data", "MainWindow.pas:coverage_line_hits_data" };
    String lineHits_values[] = { "32=1;33=1", "13=1;14=0" };

    for (int i = 0; i < coverage_names.length; ++i) { // % of coverage
      assertEquals(coverage_values[i], context.getMeasure(coverage_names[i]).getValue(), 0.0);
      assertEquals(lineHits_values[i], context.getMeasure(lineHits_names[i]).getData());
    }
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    return new FlatXmlDataSet(DelphiUtils.getResource(DB_FILE));
  }

  @Override
  protected DatabaseOperation getSetUpOperation() throws Exception {
    return DatabaseOperation.REFRESH;
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.NONE;
  }
}
