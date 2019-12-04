package org.sonar.plugins.delphi.file;

import java.util.Set;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;

public class DefaultDelphiFileConfig implements DelphiFileConfig {
  private final String encoding;
  private final SearchPath searchPath;
  private final Set<String> definitions;
  private boolean skipImplementation;

  DefaultDelphiFileConfig(String encoding, SearchPath searchPath, Set<String> definitions) {
    this.encoding = encoding;
    this.searchPath = searchPath;
    this.definitions = definitions;
  }

  @Override
  public String getEncoding() {
    return encoding;
  }

  @Override
  public SearchPath getSearchPath() {
    return searchPath;
  }

  @Override
  public Set<String> getDefinitions() {
    return definitions;
  }

  @Override
  public boolean shouldSkipImplementation() {
    return skipImplementation;
  }

  @Override
  public void setShouldSkipImplementation(boolean skipImplementation) {
    this.skipImplementation = skipImplementation;
  }
}
