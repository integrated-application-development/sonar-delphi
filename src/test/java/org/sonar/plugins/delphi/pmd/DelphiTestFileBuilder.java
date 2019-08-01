package org.sonar.plugins.delphi.pmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public abstract class DelphiTestFileBuilder<T extends DelphiTestFileBuilder<T>> {
  private static final Logger LOG = Loggers.get(DelphiTestFileBuilder.class);
  private final StringBuilder declaration = new StringBuilder();
  private final StringBuilder implementation = new StringBuilder();

  private int declCount;

  public T appendDecl(String value) {
    declaration.append(value).append("\n");
    declCount++;
    return getThis();
  }

  public T appendImpl(String value) {
    implementation.append(value).append("\n");
    return getThis();
  }

  public String getDeclaration() {
    return declaration.toString();
  }

  public String getImplementation() {
    return implementation.toString();
  }

  public File buildFile(File baseDir) {
    StringBuilder source = getSourceCode();

    try {
      File file = File.createTempFile(getFilenamePrefix(), getFileExtension(), baseDir);
      file.deleteOnExit();

      try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
        fileWriter.write(source.toString());
        fileWriter.flush();
      }
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public StringBuilder getSourceCode() {
    return getSourceCode(false);
  }

  public StringBuilder getSourceCode(boolean print) {
    StringBuilder source = generateSourceCode();

    if (print) {
      printSourceCode(source);
    }

    return source;
  }

  private void printSourceCode(StringBuilder source) {
    Reader reader = new StringReader(source.toString());
    BufferedReader lineReader = new BufferedReader(reader);
    String line;
    int lineNumber = 0;
    try {
      while ((line = lineReader.readLine()) != null) {
        LOG.info(String.format("%03d %s", ++lineNumber, line));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to print source code.", e);
    }
  }

  protected int getDeclCount() {
    return declCount;
  }

  public abstract int getOffsetDecl();

  public abstract int getOffSet();

  protected abstract T getThis();

  protected abstract StringBuilder generateSourceCode();

  protected abstract String getFilenamePrefix();

  protected abstract String getFileExtension();
}
