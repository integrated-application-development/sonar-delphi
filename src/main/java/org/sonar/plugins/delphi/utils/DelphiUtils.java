/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.core.DelphiLanguage;

/** Some utilities */
public final class DelphiUtils {
  private DelphiUtils() {}

  /**
   * Normalizes file name, changes all '\' into '/'
   *
   * @param fileName file name to normalize
   * @return normalized file name
   */
  public static String normalizeFileName(String fileName) {
    return fileName.replaceAll("\\\\", "/");
  }

  /**
   * Gets the resource from project workspace
   *
   * @param fileName Resource file name
   * @return Resource file
   */
  public static File getResource(String fileName) {
    URL url = DelphiUtils.class.getResource(fileName);
    if (url == null) {
      throw new IllegalStateException("Resource file not found: " + fileName);
    }
    String fName = url.getPath();
    File file = new File(fName);
    if (!file.exists()) {
      fName = fName.replaceAll("%20", " ");
      file = new File(fName);
    }
    return file;
  }

  /**
   * Checking value range
   *
   * @param value Value to check
   * @param min Minimum range
   * @param max Maximum range
   * @return Trims value to range, then returns it
   */
  public static int checkIntRange(int value, int min, int max) {
    int newValue = value;
    if (value < min) {
      newValue = min;
    } else if (value > max) {
      newValue = max;
    }
    return newValue;
  }

  /**
   * Gets FileFilter associated with DelphiLanguage source files (*.pas, *.dpr, *.dpk)
   *
   * @return FileFilter
   */
  public static FileFilter getFileFilter() {
    return pathname -> pathname.isFile() && acceptFile(pathname.getAbsolutePath());
  }

  /**
   * Accept file based on file extension.
   *
   * @param fileName The file name
   * @return True if the file has a valid extension
   */
  public static boolean acceptFile(String fileName) {
    String[] endings = DelphiLanguage.instance.getFileSuffixes();
    for (String ending : endings) {
      if (fileName.toLowerCase().endsWith("." + ending)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets FileFilter associated with directories
   *
   * @return FileFilter
   */
  public static FileFilter getDirectoryFilter() {
    return File::isDirectory;
  }

  /**
   * Counts the number of substring in a string
   *
   * @param string String in which to look for substrings
   * @param sub Substring to look for in a string
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
   * @param root Root directory
   * @param path Pathname to resolve
   * @return Resolved file
   */
  public static File resolveAbsolutePath(String root, String path) {
    File file = new File(normalizeFileName(path));

    if (!file.isAbsolute()) {
      String rootPath = normalizeFileName(root);
      if (!rootPath.endsWith("/")) {
        rootPath = rootPath.concat("/");
      }
      file = new File(rootPath + path);
    }

    return file;
  }

  /**
   * Resolves ..\ in a path to a file, backtraces the currentDir the number of '..' in a path.
   * Example: currentDir = 'C:\my\dir' fileName = '..\file.txt'; return = 'C:/my/file.txt'
   *
   * @param currentDir Current directory of a file
   * @param fileName File name
   * @return Resolved file name
   */
  public static String resolveBacktracePath(String currentDir, String fileName) {
    String result = normalizeFileName(fileName);
    // number of '..' in file name
    int dotdotCount = DelphiUtils.countSubstrings(result, "..");
    // get rid of '../'
    result = result.replaceAll("\\.\\./", "");

    for (int i = 0; i < dotdotCount; ++i) {
      currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
    }

    return currentDir + "/" + result;
  }

  /**
   * Reads file contents to string, transform it to lowercase
   *
   * @param file File to be read
   * @param encoding File content encoding
   * @return File content read to a string
   * @throws IllegalArgumentException When file is null
   * @throws IOException When file does not exist
   */
  public static String readFileContent(File file, String encoding) throws IOException {
    if (file == null) {
      throw new IllegalArgumentException("Passed a null file to readFileContent");
    }

    return FileUtils.readFileToString(file, encoding).replace("\uFEFF", "");
  }

  public static String getRelativePath(File file, List<File> dirs) {
    List<String> stack = new ArrayList<>();
    String path = FilenameUtils.normalize(file.getAbsolutePath());
    File cursor = new File(path);
    while (cursor != null) {
      if (containsFile(dirs, cursor)) {
        return StringUtils.join(stack, "/");
      }
      stack.add(0, cursor.getName());
      cursor = cursor.getParentFile();
    }
    return null;
  }

  private static boolean containsFile(List<File> dirs, File cursor) {
    for (File dir : dirs) {
      if (FilenameUtils.equalsNormalizedOnSystem(dir.getAbsolutePath(), cursor.getAbsolutePath())) {
        return true;
      }
    }
    return false;
  }

  public static String uriToAbsolutePath(URI uri) {
    String path = uri.getPath();
    if (":".equals(path.substring(2, 3))) {
      return path.substring(1);
    } else {
      return path;
    }
  }
}
