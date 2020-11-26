package org.sonar.plugins.delphi.preprocessor.search;

import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SearchPathTest {
  @Test
  void testSearchWithInvalidPathShouldNotThrowException() {
    SearchPath searchPath = SearchPath.create(Collections.emptyList());
    Path invalidPath = Path.of("C:/MY/INVALID/PATH");
    searchPath.search("file", invalidPath);
  }
}
