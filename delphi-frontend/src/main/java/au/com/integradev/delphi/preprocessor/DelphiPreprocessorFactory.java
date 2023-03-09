package au.com.integradev.delphi.preprocessor;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFileConfig;

public final class DelphiPreprocessorFactory {
  private final Platform platform;

  public DelphiPreprocessorFactory(Platform platform) {
    this.platform = platform;
  }

  public DelphiPreprocessor createPreprocessor(DelphiLexer lexer, DelphiFileConfig config) {
    return new DelphiPreprocessor(lexer, config, platform);
  }
}
