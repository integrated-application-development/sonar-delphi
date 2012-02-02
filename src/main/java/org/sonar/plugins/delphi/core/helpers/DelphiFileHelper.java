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
package org.sonar.plugins.delphi.core.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Helper class doing file operations
 */
public class DelphiFileHelper {

  protected Configuration configuration;

  /**
   * ctor
   * 
   * @param conf
   *          Configuration
   */
  public DelphiFileHelper(Configuration conf) {
    configuration = conf;
  }

  /**
   * Is file in excluded list?
   * 
   * @param delphiFile
   *          File to check
   * @param excludedSources
   *          Excluded paths
   * @return True if file is excluded, false otherwise
   */
  public boolean isExcluded(String fileName, List<File> excludedSources) {
    if (excludedSources == null) {
      return false;
    }
    for (File excludedDir : excludedSources) {
      String normalizedFileName = DelphiUtils.normalizeFileName(fileName.toLowerCase());
      String excludedDirNormalizedPath = DelphiUtils.normalizeFileName(excludedDir.getAbsolutePath().toLowerCase());
      if (normalizedFileName.startsWith(excludedDirNormalizedPath) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is file excluded?
   * 
   * @param delphiFile
   *          File to check
   * @param excludedSources
   *          List of excluded sources
   * @return True if file is excluded, false otherwise
   */
  public boolean isExcluded(File delphiFile, List<File> excludedSources) {
    return isExcluded(delphiFile.getAbsolutePath(), excludedSources);
  }

  /**
   * Gets code coverage excluded directories
   * 
   * @return List of excluded directories, empty list if none
   */
  public List<File> getCodeCoverageExcludedDirectories(Project project) {
    List<File> list = new ArrayList<File>();
    if (configuration == null) {
      return list;
    }

    String[] sources = configuration.getStringArray(DelphiPlugin.CC_EXCLUDED_KEY);
    if (sources == null || sources.length == 0) {
      return list;
    }
    ProjectFileSystem fileSystem = project.getFileSystem();
    for (String path : sources) {
      if (StringUtils.isEmpty(path)) {
        continue;
      }
      File excluded = DelphiUtils.resolveAbsolutePath(fileSystem.getBasedir().getAbsolutePath(), path.trim());
      if ( !excluded.exists()) {
        DelphiUtils.LOG.warn("Excluded code coverage path does not exist: " + excluded.getAbsolutePath());
      } else if ( !excluded.isDirectory()) {
        DelphiUtils.LOG.warn("Excluded code coverage path is not a directory: " + excluded.getAbsolutePath());
      } else {
        list.add(excluded);
      }
    }
    return list;
  }

  /**
   * Get unit test directories
   * 
   * @return Test directories, or empty list
   */
  public List<File> getTestDirectories(Project project) {
    
    List<File> testDirs = project.getFileSystem().getTestDirs();
    for(File directory : testDirs) {
      if(!directory.exists()) {
        DelphiUtils.LOG.warn("Test path does not exist: " + directory.getAbsolutePath());
      }
    }
    
    return testDirs;
    /*
    List<File> result = new ArrayList<File>();
    if (configuration == null || project == null) {
      return result;
    }
    String[] dirs = configuration.getStringArray(DelphiPlugin.TEST_DIRECTORIES_KEY);
    if (dirs.length == 0) {
      return result;
    }
    String mainPath = project.getFileSystem().getBasedir().getAbsolutePath();
    for (String path : dirs) {
      if (path.isEmpty()) {
        continue;
      }
      File testDir = DelphiUtils.resolveAbsolutePath(mainPath, path.trim());
      if ( !testDir.exists()) {
        DelphiUtils.LOG.warn("Test path does not exist: " + testDir.getAbsolutePath());
        DelphiUtils.getDebugLog().println("Test path does not exist: " + testDir.getAbsolutePath());
        continue;
      }
      result.add(testDir);
    }

    return result; */
  }

  /**
   * Checks is providen file is a unit test file
   * 
   * @param delphiFile
   *          File to check
   * @param testDirectories
   *          Test directories
   * @return True if file is unit test, false otherwise
   */
  public boolean isTestFile(File delphiFile, List<File> testDirectories) {
    if (delphiFile == null) {
      throw new IllegalStateException("No file passed to DelphiLanguage::isTestFile()");
    } else if (testDirectories == null || testDirectories.size() == 0) {
      return false;
    }
    for (File directory : testDirectories) {
      if (directory.exists() && delphiFile.getAbsolutePath().toLowerCase().startsWith(directory.getAbsolutePath().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

}
