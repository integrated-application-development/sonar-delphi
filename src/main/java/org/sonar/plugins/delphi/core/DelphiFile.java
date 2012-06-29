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
package org.sonar.plugins.delphi.core;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * File for DelphiLanguage language to be parsed (*.pas)
 */
public class DelphiFile extends Resource<DelphiPackage> {

  private String filename;
  private String longName;
  private String path = "";
  private String packageKey;
  private boolean unitTest = false;
  private DelphiPackage parent = null;

  /**
   * SONARPLUGINS-687: For backward compatibility
   */
  public DelphiFile(String key) {
    this(key, false);
  }

  /**
   * Ctor
   * 
   * @param key
   *          Resource key
   * @param unitTest
   *          If in unit tests
   */
  public DelphiFile(String key, boolean unitTest) {
    super();
    if (key != null && key.indexOf('$') >= 0) {
      throw new IllegalArgumentException("DelphiLanguage inner classes are not supported : " + key);
    }
    String realKey = StringUtils.trim(key);
    this.unitTest = unitTest;

    if (realKey.contains(".")) {
      this.filename = StringUtils.substringAfterLast(realKey, ".");
      this.packageKey = StringUtils.substringBeforeLast(realKey, ".");
      this.longName = realKey;

    } else {
      this.filename = realKey;
      this.longName = realKey;
      this.packageKey = DelphiPackage.DEFAULT_PACKAGE_NAME;
      realKey = new StringBuilder().append(DelphiPackage.DEFAULT_PACKAGE_NAME).append(".").append(realKey).toString();
    }
    setKey(realKey);
  }

  /**
   * Ctor
   * 
   * @param filePackageKey
   *          Package name
   * @param className
   *          Class name
   * @param unitTest
   *          If in unit tests
   */
  public DelphiFile(String filePackageKey, String className, boolean unitTest) {
    super();
    if (className != null) {
      this.filename = className.trim();
      this.unitTest = unitTest;
      
      String key;
      if (StringUtils.isBlank(filePackageKey)) {
        this.packageKey = DelphiPackage.DEFAULT_PACKAGE_NAME;
        this.longName = this.filename;
        key = new StringBuilder().append(this.packageKey).append(".").append(this.filename).toString();
      } else {
        this.packageKey = filePackageKey.trim();
        key = new StringBuilder().append(this.packageKey).append(".").append(this.filename).toString();
        this.longName = key;
      }
      
      setKey(key);
    }
    else {
      throw new IllegalArgumentException("DelphiFile className cannot be null");
    }
  }

  @Override
  public DelphiPackage getParent() {
    if (parent == null) {
      parent = new DelphiPackage(packageKey);
    }
    return parent;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Language getLanguage() {
    return DelphiLanguage.instance;
  }

  @Override
  public String getName() {
    return filename;
  }

  @Override
  public String getLongName() {
    return longName;
  }

  @Override
  public String getScope() {
    return Resource.SCOPE_ENTITY;
  }

  @Override
  public String getQualifier() {
    return unitTest ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_CLASS;
  }

  /**
   * @return True if used in unit tests, false othwerwise
   */
  public boolean isUnitTest() {
    return unitTest;
  }

  /**
   * @return File absolute path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets file path
   * 
   * @param newPath
   *          New file path
   */
  public void setPath(String newPath) {
    path = newPath;
  }

  @Override
  public boolean matchFilePattern(String antPattern) {
    String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
    WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
    return matcher.match(getKey());
  }

  /**
   * SONARPLUGINS-687: For backward compatibility
   */
  public static DelphiFile fromIOFile(File file, List<File> sourceDirs) {
    return fromIOFile(file, sourceDirs, false);
  }

  /**
   * Creates a {@link DelphiFile} from a file in the source directories.
   * 
   * @param unitTest
   *          whether it is a unit test file or a source file
   * @return the {@link DelphiFile} created if exists, null otherwise
   */

  public static DelphiFile fromIOFile(File file, List<File> sourceDirs, boolean unitTest) {
    if (file == null) {
      return null;
    }

    String relativePath = DefaultProjectFileSystem.getRelativePath(file, sourceDirs);    
    if (relativePath != null) {
      String packageName = null;
      String className = relativePath;

      if (relativePath.indexOf('/') >= 0) {
        packageName = StringUtils.substringBeforeLast(relativePath, "/");
        packageName = StringUtils.replace(packageName, "/", ".");
        className = StringUtils.substringAfterLast(relativePath, "/");
      }
      className = StringUtils.substringBeforeLast(className, ".");
      DelphiFile newFile = new DelphiFile(packageName, className, unitTest);
       
      newFile.setPath( DelphiUtils.normalizeFileName(file.getAbsolutePath()) );
      return newFile;
    }
    return null;
  }

  /**
   * Shortcut to {@link #fromIOFile(File, List, boolean)} with an absolute path.
   */
  public static DelphiFile fromAbsolutePath(String path, List<File> sourceDirs, boolean unitTest) {
    if (path == null) {
      return null;
    }
    return fromIOFile(new File(path), sourceDirs, unitTest);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("key", getKey()).append("package", packageKey).append("longName", longName)
        .append("unitTest", unitTest).toString();
  }

  /**
   * Gets FileFilter associated with DelphiLanguage source files (*.pas, *.dpr, *.dpk)
   * 
   * @return FileFilter
   */
  public static FileFilter getFileFilter() {
    return new FileFilter() {

      public boolean accept(File pathname) {
        if ( !pathname.isFile()) {
          return false;
        }
        String[] endings = DelphiLanguage.instance.getFileSuffixes();
        for (String ending : endings) {
          if (pathname.getAbsolutePath().endsWith("." + ending)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Gets FileFilter associated with directories
   * 
   * @return FileFilter
   */
  public static FileFilter getDirectoryFilter() {
    return new FileFilter() {

      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    };
  }

}
