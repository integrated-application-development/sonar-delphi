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
package org.sonar.plugins.delphi.surefire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.api.AbstractSurefireParser;

/**
 * Parses unit test reports from XML file.
 */
public class DelphiSureFireParser extends AbstractSurefireParser {

  private static final String FILE_EXT = ".pas";
  private static final String ERROR_MSG = "Unit test file not found: ";
  private Project project;
  private SensorContext context;
  private List<File> testSources;

  /**
   * ctor
   * 
   * @param delphiProject
   *          project provided by Sonar
   * @param sensorContext
   *          sensor context provided by Sonar
   */
  public DelphiSureFireParser(Project delphiProject, SensorContext sensorContext) {
    project = delphiProject;
    context = sensorContext;
    testSources = DelphiProjectHelper.getInstance().getTestDirectories(project);
  }

  @Override
  protected Resource<?> getUnitTestResource(String classKey) {
    try {
      if (testSources == null) {
        return new DelphiFile(classKey, true); // default behavior
      }
      for (File parent : testSources) {
        String path = parent.getAbsolutePath() + "\\" + classKey + FILE_EXT;
        File testFile = new File(path);
        if ( !testFile.exists()) {
          continue; // look further
        }
        if (testFile != null) {
          DelphiFile resourceFile = DelphiFile.fromIOFile(testFile, project.getFileSystem().getSourceDirs(), true);
          if (resourceFile != null) {
            // resource source code not saved, because tests files were
            // excluded from analysis, so read the test file and save its source code
            // so Sonar could show it
            if ( !context.isIndexed(resourceFile, true) && DelphiProjectHelper.getInstance().getImportSources()) {
              BufferedReader br = new BufferedReader(new FileReader(testFile));
              StringBuilder source = new StringBuilder();
              String line;
              while ((line = br.readLine()) != null) {
                source.append(line).append('\n');
              }
              br.close();
              context.saveSource(resourceFile, source.toString());
            }

            return resourceFile;
          }
        }
      } // for

      throw new FileNotFoundException(); // no file found

    } catch (FileNotFoundException e) {
      DelphiUtils.LOG.warn(ERROR_MSG + classKey + FILE_EXT);
      DelphiUtils.getDebugLog().println(ERROR_MSG + classKey + FILE_EXT);
      return new DelphiFile(classKey, true); // default behavior
    } catch (IOException e) {
      DelphiUtils.LOG.warn(ERROR_MSG + classKey + FILE_EXT);
      DelphiUtils.getDebugLog().println(ERROR_MSG + classKey + FILE_EXT);
      return new DelphiFile(classKey, true); // default behavior
    }
  }

}