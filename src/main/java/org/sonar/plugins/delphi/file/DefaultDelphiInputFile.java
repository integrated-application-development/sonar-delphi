package org.sonar.plugins.delphi.file;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DefaultDelphiInputFile extends DefaultDelphiFile implements DelphiInputFile {
  private InputFile inputFile;

  @Override
  public InputFile getInputFile() {
    return inputFile;
  }

  void setInputFile(InputFile inputFile) {
    this.inputFile = inputFile;
  }
}
