package org.sonar.plugins.delphi.file;

import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public class DefaultDelphiFileConfig implements DelphiFileConfig {
  private final String encoding;
  private final TypeFactory typeFactory;
  private final SearchPath searchPath;
  private final Set<String> definitions;
  private final boolean skipImplementation;

  DefaultDelphiFileConfig(
      String encoding,
      TypeFactory typeFactory,
      SearchPath searchPath,
      Set<String> definitions,
      boolean skipImplementation) {
    this.encoding = encoding;
    this.typeFactory = typeFactory;
    this.searchPath = searchPath;
    this.definitions = definitions;
    this.skipImplementation = skipImplementation;
  }

  @Nullable
  @Override
  public String getEncoding() {
    return encoding;
  }

  @Override
  public TypeFactory getTypeFactory() {
    return typeFactory;
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
}
