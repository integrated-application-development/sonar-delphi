/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.msbuild.expression;

import com.google.common.base.Suppliers;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.io.FilenameUtils;

public class MSBuildWellKnownPropertyHelper {
  private final WellKnownFile project;
  private final WellKnownFile thisFile;

  public MSBuildWellKnownPropertyHelper(String thisFilePath, String projectPath) {
    this.project = new WellKnownFile(projectPath);
    this.thisFile = new WellKnownFile(thisFilePath);
  }

  private static class WellKnownFile {
    private final Supplier<String> extension;
    private final Supplier<String> file;
    private final Supplier<String> fullPath;
    private final Supplier<String> name;
    private final Supplier<String> directory;
    private final Supplier<String> directoryNoRoot;

    public WellKnownFile(String path) {
      this.extension =
          Suppliers.memoize(
              () -> {
                var ext = FilenameUtils.getExtension(path);
                if (Objects.equals(ext, "")) {
                  return "";
                }
                return "." + ext;
              });
      this.file = Suppliers.memoize(() -> FilenameUtils.getName(path));
      this.fullPath = Suppliers.memoize(() -> path);
      this.name = Suppliers.memoize(() -> FilenameUtils.getBaseName(path));
      this.directory = Suppliers.memoize(() -> FilenameUtils.getFullPath(fullPath.get()));
      this.directoryNoRoot =
          Suppliers.memoize(
              () -> {
                var dir = directory.get();
                return dir.substring(FilenameUtils.getPrefixLength(dir));
              });
    }

    public String getExtension() {
      return extension.get();
    }

    public String getFile() {
      return file.get();
    }

    public String getFullPath() {
      return fullPath.get();
    }

    public String getName() {
      return name.get();
    }

    public String getDirectory() {
      return directory.get();
    }

    public String getDirectoryNoRoot() {
      return directoryNoRoot.get();
    }
  }

  public String getProperty(String name) {
    // Well-known metadata is automatically populated by MSBuild. We can't support all of them,
    // but the ones that are simple transformations are relatively easy for us to calculate.
    // https://learn.microsoft.com/en-us/visualstudio/msbuild/msbuild-reserved-and-well-known-properties

    switch (name.toLowerCase()) {
      case "os":
        return "Windows_NT";
      case "msbuildthisfilename":
        return thisFile.getName();
      case "msbuildthisfilefullpath":
        return thisFile.getFullPath();
      case "msbuildthisfileextension":
        return thisFile.getExtension();
      case "msbuildthisfiledirectorynoroot":
        return thisFile.getDirectoryNoRoot();
      case "msbuildthisfiledirectory":
        return thisFile.getDirectory();
      case "msbuildthisfile":
        return thisFile.getFile();
      case "msbuildprojectname":
        return project.getName();
      case "msbuildprojectfullpath":
        return project.getFullPath();
      case "msbuildprojectfile":
        return project.getFile();
      case "msbuildprojectextension":
        return project.getExtension();
      case "msbuildprojectdirectory":
        return project.getDirectory();
      case "msbuildprojectdirectorynoroot":
        return project.getDirectoryNoRoot();
      default:
        return null;
    }
  }
}
