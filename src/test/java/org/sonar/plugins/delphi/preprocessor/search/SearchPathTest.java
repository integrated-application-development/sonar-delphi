package org.sonar.plugins.delphi.preprocessor.search;

import java.nio.file.Path;
import java.util.Collections;
import org.junit.Test;

public class SearchPathTest {
  @Test
  public void testSearchWithInvalidPathShouldNotThrowException() {
    SearchPath searchPath = SearchPath.create(Collections.emptyList());
    Path invalidPath = Path.of("C:/MY/INVALID/PATH");
    searchPath.search("file", invalidPath);
  }
}
