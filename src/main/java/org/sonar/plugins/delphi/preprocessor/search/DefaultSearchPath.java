package org.sonar.plugins.delphi.preprocessor.search;

import static java.util.Comparator.comparingInt;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class DefaultSearchPath implements SearchPath {
  private static final Logger LOG = Loggers.get(DefaultSearchPath.class);
  private final Set<Path> rootDirectories;
  private final SetMultimap<Path, Path> filesByDirectory;
  private final Set<Path> allFiles;
  private final Set<Path> indexedPaths;

  DefaultSearchPath(Iterable<Path> searchDirectories) {
    this.rootDirectories = ImmutableSet.copyOf(searchDirectories);
    this.filesByDirectory = HashMultimap.create();
    this.allFiles = new HashSet<>();
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
  public Set<Path> sourceFiles() {
    return allFiles;
  }

  private Set<Path> indexIncludePath(Path path) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for (Path child : stream) {
        if (Files.isDirectory(child)) {
          filesByDirectory.putAll(path, indexIncludePath(child));
        } else {
          filesByDirectory.put(path, child);
          allFiles.add(child);
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
