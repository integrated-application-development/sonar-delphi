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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonarsource.analyzer.commons.ProgressReport;

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
    return fileName.replace("\\", "/");
  }

  /**
   * Gets the resource from project workspace
   *
   * @param fileName Resource file name
   * @return Resource file
   */
  public static File getResource(String fileName) {
    URL url = DelphiUtils.class.getResource(fileName);
    File file = FileUtils.toFile(url);
    return Objects.requireNonNull(file, "Resource not found: " + fileName);
  }

  /**
   * Accept file based on file extension.
   *
   * @param file The file
   * @return True if the file has a valid extension
   */
  public static boolean acceptFile(Path file) {
    return acceptFile(file.toString());
  }

  /**
   * Accept file based on file extension.
   *
   * @param fileName The file name
   * @return True if the file has a valid extension
   */
  public static boolean acceptFile(String fileName) {
    String extension = FilenameUtils.getExtension(fileName);
    String[] acceptableExtensions = DelphiLanguage.instance.getFileSuffixes();
    return Arrays.stream(acceptableExtensions)
        .anyMatch(acceptable -> acceptable.equalsIgnoreCase(extension));
  }

  /**
   * Adds root directory to path if path is relative, or returns path if absolute
   *
   * @param root Root directory
   * @param path Pathname to resolve
   * @return Resolved file
   */
  public static File resolveAbsolutePath(String root, String path) {
    File file = new File(path);

    if (!file.isAbsolute()) {
      if (!root.endsWith(File.separator)) {
        root = root.concat(File.separator);
      }
      file = new File(root + path);
    }

    return file;
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
  public static String readFileContent(@NotNull File file, String encoding) throws IOException {
    return FileUtils.readFileToString(file, encoding);
  }

  public static String uriToAbsolutePath(URI uri) {
    String path = uri.getPath();
    if (":".equals(path.substring(2, 3))) {
      path = path.substring(1);
    }
    return path;
  }

  public static List<Path> inputFilesToPaths(Iterable<InputFile> inputFiles) {
    List<Path> result = new ArrayList<>();
    for (InputFile inputFile : inputFiles) {
      result.add(inputFileToPath(inputFile));
    }
    return result;
  }

  public static Path inputFileToPath(InputPath inputFile) {
    return Paths.get(inputFile.uri());
  }

  public static Path resolvePathFromBaseDir(Path baseDir, Path path) {
    if (!path.isAbsolute()) {
      path = Path.of(baseDir.toAbsolutePath().toString(), path.toString());
    }
    return path.toAbsolutePath().normalize();
  }

  @Nullable
  public static Path commonPath(Path pathA, Path pathB) {
    pathA = pathA.normalize();
    pathB = pathB.normalize();

    Path common = null;
    if (pathA.isAbsolute() == pathB.isAbsolute()) {
      if (!pathA.isAbsolute()) {
        common = Paths.get("");
      } else if (pathA.getRoot() == null || pathB.getRoot() == null) {
        common = Paths.get("/");
      } else if (pathA.getRoot().equals(pathB.getRoot())) {
        common = pathA.getRoot();
      }
    }

    if (common != null) {
      int nameCount = Math.min(pathA.getNameCount(), pathB.getNameCount());
      for (int i = 0; i < nameCount; i++) {
        if (!pathA.getName(i).equals(pathB.getName(i))) {
          break;
        }
        common = common.resolve(pathA.getName(i));
      }
    }

    return common;
  }

  public static void stopProgressReport(ProgressReport progressReport, boolean success) {
    if (success) {
      progressReport.stop();
    } else {
      progressReport.cancel();
    }
  }
}
