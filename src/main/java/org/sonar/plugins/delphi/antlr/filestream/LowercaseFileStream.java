package org.sonar.plugins.delphi.antlr.filestream;

import java.io.IOException;
import org.antlr.runtime.ANTLRFileStream;

/** Provides a case-insensitive file stream without any pre-processing. */
public class LowercaseFileStream extends ANTLRFileStream {

  public LowercaseFileStream(String fileName, String encoding) throws IOException {
    super(fileName, encoding);
  }

  /** Overrides AntlrFileStream LookAhead for case insensitivity. */
  @Override
  public int LA(int i) {
    int la = super.LA(i);

    return Character.toLowerCase(la);
  }

  @Override
  public String getSourceName() {
    return this.fileName;
  }
}
