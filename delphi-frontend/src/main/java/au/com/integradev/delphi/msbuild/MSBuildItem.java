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
package au.com.integradev.delphi.msbuild;

import com.google.common.base.Suppliers;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.apache.commons.io.FilenameUtils;

public class MSBuildItem {
  private final String identity;
  private final String projectDirectory;
  private final Supplier<Path> path;
  private final Supplier<String> fullPath;
  private final Supplier<String> rootDir;
  private final Supplier<String> fileName;
  private final Supplier<String> extension;
  private final Supplier<String> relativeDir;
  private final Supplier<String> directory;
  private final Map<String, String> customMetadata;

  public MSBuildItem(String identity, String projectDirectory, Map<String, String> customMetadata) {
    this.identity = identity;
    this.projectDirectory = projectDirectory;
    this.customMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.customMetadata.putAll(customMetadata);

    path = Suppliers.memoize(this::generatePath);
    fullPath = Suppliers.memoize(this::resolve);
    rootDir = Suppliers.memoize(this::rootDir);
    fileName = Suppliers.memoize(this::fileName);
    extension = Suppliers.memoize(this::extension);
    relativeDir = Suppliers.memoize(this::relativeDir);
    directory = Suppliers.memoize(this::directory);
  }

  public String getIdentity() {
    return identity;
  }

  public Path getPath() {
    return path.get();
  }

  private Path generatePath() {
    return Path.of(fullPath.get());
  }

  public String getMetadata(String name) {
    // Well-known metadata is automatically populated by MSBuild. We can't support all of them,
    // but the ones that are simple transformations are relatively easy for us to calculate.
    // https://learn.microsoft.com/en-us/visualstudio/msbuild/msbuild-well-known-item-metadata
    switch (name.toLowerCase()) {
      case "fullpath":
        return fullPath.get();
      case "rootdir":
        return rootDir.get();
      case "filename":
        return fileName.get();
      case "extension":
        return extension.get();
      case "relativedir":
        return relativeDir.get();
      case "directory":
        return directory.get();
      case "identity":
        return identity;
      default:
        return customMetadata.getOrDefault(name, "");
    }
  }

  private String resolve() {
    return FilenameUtils.concat(projectDirectory, identity);
  }

  private String rootDir() {
    return FilenameUtils.getPrefix(fullPath.get());
  }

  private String fileName() {
    return FilenameUtils.getBaseName(identity);
  }

  private String extension() {
    String ext = FilenameUtils.getExtension(identity);
    if (Objects.equals(ext, "")) {
      return "";
    }
    return "." + ext;
  }

  private String relativeDir() {
    return FilenameUtils.getFullPath(identity);
  }

  private String directory() {
    return FilenameUtils.getPath(fullPath.get());
  }
}
