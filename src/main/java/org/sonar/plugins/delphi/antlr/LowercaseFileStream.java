package org.sonar.plugins.delphi.antlr;

import java.io.IOException;
import org.antlr.runtime.ANTLRFileStream;

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
}
