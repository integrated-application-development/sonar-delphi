package org.sonar.plugins.delphi.file;

import java.util.Set;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;

public interface DelphiFileConfig {
  /**
   * Returns the encoding that the source file is expected to be
   *
   * @return Name of encoding
   */
  String getEncoding();

  /**
   * Returns the search path where include files can be found and imports can be resolved
   *
   * @return Search path
   */
  SearchPath getSearchPath();

  /**
   * Returns a list of symbol definitions used in conditional compiler directives
   *
   * @return Symbol definitions
   */
  Set<String> getDefinitions();

  /**
   * Returns whether the implementation section can be skipped when lexing the file.
   *
   * <p>NOTE: In some circumstances, this will not be logically possible. For example, if the
   * implementation section is nested inside of a branching conditional directive then the
   * implementation section will not be skipped.
   *
   * @return true if the implementation should be skipped when parsing the file
   */
  boolean shouldSkipImplementation();

  /**
   * Sets whether the implementation section can be skipped when lexing the file.
   *
   * <p>NOTE: In some circumstances, this will not be logically possible. For example, if the
   * implementation section is nested inside of a branching conditional directive then the
   * implementation section will not be skipped.
   *
   * @param skipImplementation whether the implementation should be skipped when lexing the file
   */
  void setShouldSkipImplementation(boolean skipImplementation);
}
