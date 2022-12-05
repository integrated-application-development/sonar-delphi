/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.preprocessor.search;

import static java.util.Comparator.comparingInt;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class DefaultSearchPath implements SearchPath {
  private static final Logger LOG = Loggers.get(DefaultSearchPath.class);
  private final Set<Path> rootDirectories;
  private final SetMultimap<Path, Path> filesByDirectory;
  private final Set<Path> indexedPaths;

  DefaultSearchPath(List<Path> searchDirectories) {
    this.rootDirectories = Collections.unmodifiableSet(new LinkedHashSet<>(searchDirectories));
    this.filesByDirectory = HashMultimap.create();
    this.indexedPaths = new HashSet<>();

    rootDirectories.forEach(this::indexIncludePath);
  }

  @Override
  @Nullable
  public Path search(String filename, Path includePath) {
    if (!indexedPaths.contains(includePath)) {
      indexIncludePath(includePath);
    }

    Path path = findFileForPath(filename, includePath);
    if (path == null) {
      for (Path root : rootDirectories) {
        path = findFileForPath(filename, root);
        if (path != null) {
          break;
        }
      }
    }

    return path;
  }

  @Override
  public Set<Path> getRootDirectories() {
    return rootDirectories;
  }

  private Set<Path> indexIncludePath(Path path) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for (Path child : stream) {
        if (Files.isDirectory(child)) {
          filesByDirectory.putAll(path, indexIncludePath(child));
        } else {
          filesByDirectory.put(path, child);
        }
      }
    } catch (IOException e) {
      LOG.info("Failed to index include path directory '{}'", path);
      LOG.debug("Error while indexing search path:", e);
    }

    indexedPaths.add(path);
    return filesByDirectory.get(path);
  }

  private Path findFileForPath(String filename, Path path) {
    return filesByDirectory.get(path).stream()
        .filter(file -> filename.equalsIgnoreCase(file.getFileName().toString()))
        .min(comparingInt(Path::getNameCount))
        .orElse(null);
  }
}
