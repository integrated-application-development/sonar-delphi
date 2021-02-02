package org.sonar.plugins.delphi.preprocessor.search;

import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nullable;

public interface SearchPath {
  @Nullable
  Path search(String filename, Path startPath);

  Set<Path> getRootDirectories();

  static SearchPath create(Iterable<Path> searchDirectories) {
    return new DefaultSearchPath(searchDirectories);
  }
}
