package org.sonar.plugins.delphi.antlr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.antlr.runtime.ANTLRStringStream;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

public class DelphiFileStream extends ANTLRStringStream {
  private final String fileName;

  public DelphiFileStream(String fileName, String encoding) throws IOException {
    this.fileName = fileName;
    this.load(fileName, encoding);
  }

  private void load(String fileName, String encoding) throws IOException {
    if (fileName != null) {
      File f = new File(fileName);
      int size = (int) f.length();
      try (BOMInputStream input =
          new BOMInputStream(
              new FileInputStream(fileName),
              false,
              ByteOrderMark.UTF_8,
              ByteOrderMark.UTF_16LE,
              ByteOrderMark.UTF_16BE,
              ByteOrderMark.UTF_32LE,
              ByteOrderMark.UTF_32BE)) {
        if (encoding == null) {
          encoding = input.getBOMCharsetName();
        }

        if (encoding == null) {
          encoding = Charset.defaultCharset().name();
        }

        try (InputStreamReader reader = new InputStreamReader(input, encoding)) {
          this.data = new char[size];
          super.n = reader.read(this.data);
        }
      }
    }
  }

  @Override
  public String getSourceName() {
    return this.fileName;
  }

  @Override
  public int LA(int i) {
    int la = super.LA(i);

    return Character.toLowerCase(la);
  }
}
