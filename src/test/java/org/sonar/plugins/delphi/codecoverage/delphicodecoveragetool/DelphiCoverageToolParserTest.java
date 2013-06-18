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
package org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiCoverageToolParserTest 
{
	  private Project project;
	  private SensorContext context;
	  private File baseDir;

	  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
	  private static final String REPORT_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/reports/Coverage.xml";

	  @Before
	  public void init() {

		context = mock(SensorContext.class);
		  
	    project = mock(Project.class);
	    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

	    baseDir = DelphiUtils.getResource(ROOT_NAME);
	    File reportDir = new File(baseDir.getAbsolutePath() + "/reports");

	    File[] dirs = baseDir.listFiles(DelphiFile.getDirectoryFilter()); // get all directories

	    List<File> sourceDirs = new ArrayList<File>(dirs.length);
	    List<File> sourceFiles = new ArrayList<File>();

	    sourceDirs.add(baseDir); // include baseDir
	    for (File source : baseDir.listFiles(DelphiFile.getFileFilter())) {
	      sourceFiles.add(source);
	    }

	    for (File directory : dirs) { // get all source files from all directories
	      File[] files = directory.listFiles(DelphiFile.getFileFilter());
	      for (File sourceFile : files) {
	        sourceFiles.add(sourceFile); // put all files to list
	      }
	      sourceDirs.add(directory); // put all directories to list
	    }

	    when(project.getLanguage()).thenReturn(DelphiLanguage.instance);
	    when(project.getFileSystem()).thenReturn(pfs);

	    when(pfs.getBasedir()).thenReturn(baseDir);
	    when(pfs.getSourceFiles(DelphiLanguage.instance)).thenReturn(sourceFiles);
	    when(pfs.getSourceDirs()).thenReturn(sourceDirs);
	    when(pfs.getReportOutputDir()).thenReturn(reportDir);
	 }
	
	
	@Test
	public void parseTest() {
		File reportFile = DelphiUtils.getResource(REPORT_FILE);
		DelphiCodeCoverageToolParser parser = new DelphiCodeCoverageToolParser(project, reportFile);		
		parser.parse(project, context);
	}
	
	
	
}
