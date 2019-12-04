package org.sonar.plugins.delphi.file;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface DelphiFileConfig {
  /**
   * Returns the encoding that the source file is expected to be
   *
   * @return Name of encoding
   */
  String getEncoding();

  /**
   * Returns a list of paths where include files can be found and imports can be resolved
   *
   * @return Search path
   */
  List<Path> getSearchPath();

  /**
   * Returns a list of symbol definitions used in conditional compiler directives
   *
   * @return Symbol definitions
   */
  Set<String> getDefinitions();
}
