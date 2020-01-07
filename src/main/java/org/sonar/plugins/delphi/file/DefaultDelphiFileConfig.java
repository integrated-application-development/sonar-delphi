package org.sonar.plugins.delphi.file;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class DefaultDelphiFileConfig implements DelphiFileConfig {
  private String encoding;
  private List<Path> searchPath;
  private Set<String> definitions;
  private boolean skipImplementation;

  DefaultDelphiFileConfig(String encoding, List<Path> searchPath, Set<String> definitions) {
    this.encoding = encoding;
    this.searchPath = searchPath;
    this.definitions = definitions;
  }

  @Override
  public String getEncoding() {
    return encoding;
  }

  @Override
  public List<Path> getSearchPath() {
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
