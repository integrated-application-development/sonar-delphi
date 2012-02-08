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
package org.sonar.plugins.delphi.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiFile;

/**
 * Class for directory filtering with File::listFiles()
 * 
 */
class DirectoryFileFilter implements FileFilter {

  public boolean accept(File pathname) {
    return pathname.isDirectory() && !".svn".equals(pathname.getName());
  }
}

/**
 * Class for file filtering with File::listFiles(), filters by file suffix
 */
class CustomFileFilter implements FileFilter {

  private String[] suffix;

  CustomFileFilter(String[] filterSuffix) {
    suffix = filterSuffix.clone();
  }

  public boolean accept(File pathname) {
    for (String ending : suffix) {
      if (pathname.getAbsolutePath().endsWith(ending)) {
        return true;
      }
    }
    return false;
  }
}

/**
 * Some utilities
 */
public final class DelphiUtils {

  private DelphiUtils() {
  } // only static methods

  /**
   * Logger class, use it for logging/debugging at Sonar window
   */
  public static final Logger LOG = LoggerFactory.getLogger(DelphiPlugin.class.getName());
  
  /**
   * Normalizes file name, changes all '\' into '/'
   * @param fileName  file name to normalize
   * @return  normalized file name
   */
  public static String normalizeFileName(String fileName) {
    return fileName.replaceAll("\\\\", "/");
  }

  /**
   * Gets the resource from project workspace
   * 
   * @param cl
   *          Class
   * @param fileName
   *          Resource file name
   * @return Resource file
   */
  public static File getResource(String fileName) {
    URL url = new DelphiUtils().getClass().getResource(fileName);
    if (url == null) {
      throw new IllegalStateException("Resource file not found: " + fileName);
    }
    String fName = url.getPath();
    File file = new File(fName);
    if ( !file.exists()) {
      fName = fName.replaceAll("%20", " ");
      file = new File(fName);
    }
    return file;
  }

  /**
   * Checking value range
   * 
   * @param value
   *          Value to check
   * @param min
   *          Minimum range
   * @param max
   *          Maximum range
   * @return Trims value to range, then returns it
   */
  public static double checkRange(double value, double min, double max) {
    double newValue = value;
    if (value < min) {
      newValue = min;
    } else if (value > max) {
      newValue = max;
    }
    return newValue;
  }

  /**
   * Gets custom file filter, the file must ends with provided suffix
   * 
   * @param suffix
   *          File suffix
   * @return CustomFileFilter
   */
  public static CustomFileFilter getCustomFileFilter(String[] suffix) {
    return new CustomFileFilter(suffix);
  }

  /**
   * Gets directory file filter, it filters out directories
   * 
   * @return DirectoryFileFilter
   */
  public static DirectoryFileFilter getDirectoryFileFilter() {
    return new DirectoryFileFilter();
  }

  /**
   * Gets source directories of a project
   * 
   * @param project
   *          Project
   * @return Source directories
   */
  public static List<File> getSourceDirectories(Project project) {
    if (project == null) {
      return null;
    }
    List<File> sourceDirs = new ArrayList<File>();
    File baseDir = project.getFileSystem().getBasedir();

    collectDirs(baseDir, sourceDirs);

    return sourceDirs;
  }

  /**
   * Gets all source files from project
   * 
   * @param project
   *          DelphiLanguage project
   * @return List of all source files (.pas)
   */
  public static List<File> getSourceFiles(Project project) {
    if (project == null) {
      return null;
    }
    List<File> sourceFiles = new ArrayList<File>();
    File baseDir = project.getFileSystem().getBasedir();

    collectFiles(baseDir, sourceFiles, DelphiFile.getFileFilter());

    return sourceFiles;
  }

  private static void collectDirs(File baseDir, List<File> source) {
    if (baseDir == null || source == null) {
      return;
    }

    FileFilter filter = new DirectoryFileFilter();
    if (filter.accept(baseDir)) {
      source.add(baseDir);
      File[] children = baseDir.listFiles(filter);
      for (File child : children) {
        collectDirs(child, source);
      }
    }
  }

  // collecting files from dir, putting them to source, only files matching provided filter
  private static void collectFiles(File baseFile, List<File> source, FileFilter filter) {
    if (baseFile == null || source == null || filter == null) {
      return;
    }

    if (filter.accept(baseFile)) {
      source.add(baseFile);
    }

    File[] content = baseFile.listFiles(filter); // get directory contents
    if (content != null) {
      for (File file : content) {
        if (filter.accept(file)) {
          source.add(file); // add current file/folder
        }
      }
    }

    File[] dirs = baseFile.listFiles(getDirectoryFileFilter());
    if (dirs == null) {
      return;
    }
    for (File dir : dirs) {
      collectFiles(dir, source, filter);
    }
  }

  /**
   * Counts the number of substring in a string
   * 
   * @param string
   *          String in which to look for substrings
   * @param sub
   *          Substring to look for in a string
   * @return The count of substrings in a string
   */
  public static int countSubstrings(String string, String sub) {
    int count = 0;
    int index = -1;
    while ((index = string.indexOf(sub, index + 1)) != -1) {
      ++count;
    }
    return count;
  }

  /**
   * Adds root directory to path if path is relative, or returns path if absolute
   * 
   * @param root
   *          Root directory
   * @param path
   *          Pathname to resolve
   * @return Resolved file
   */
  public static File resolveAbsolutePath(String root, String path) {
    File file = new File( normalizeFileName(path) );
    
    if ( !file.isAbsolute()) {
      String rootPath = normalizeFileName(root);
      if ( !rootPath.endsWith("/")) {
        rootPath = rootPath.concat("/");
      }
      file = new File(rootPath + path);
    }

    return file;
  }

  /**
   * Resolves ..\ in a path to a file, backtraces the currentDir the number of '..' in a path. Example: currentDir = 'C:\my\dir' fileName =
   * '..\file.txt'; return = 'C:/my/file.txt'
   * 
   * @param currentDir
   *          Current directory of a file
   * @param fileName
   *          File name
   * @return Resolved file name
   */
  public static String resolveBacktracePath(String currentDir, String fileName) {
    String result = normalizeFileName(fileName);
    int dotdotCount = DelphiUtils.countSubstrings(result, ".."); // number of '..' in file name
    result = result.replaceAll("\\.\\./", ""); // get rid of '../'

    for (int i = 0; i < dotdotCount; ++i) {
      currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
    }

    return currentDir + "/" + result;
  }

  /**
   * Reads file contents to string, transform it to lowercase
   * 
   * @param fileName
   *          File name
   * @param encoding
   *          File encoding
   * @return File content readed to a string
   * @throws IOException
   *           When file not found
   */
  public static String readFileContent(File f, String encoding) throws IOException {
    if (f == null || !f.exists()) {
      throw new IOException();
    }
    String fileString = null;
    int size = (int) f.length();

    InputStreamReader isr;
    FileInputStream fis = new FileInputStream(f);
    if (encoding != null) {
      isr = new InputStreamReader(fis, encoding);
    } else {
      isr = new InputStreamReader(fis);
    }

    try {
      char fileData[] = new char[size];
      isr.read(fileData);
      fileString = new String(fileData);
    } finally {
      isr.close();
    }

    return fileString;
  }

}
