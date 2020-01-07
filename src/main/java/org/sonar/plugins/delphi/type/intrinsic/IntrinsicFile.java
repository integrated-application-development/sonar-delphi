package org.sonar.plugins.delphi.type.intrinsic;

import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;

import org.sonar.plugins.delphi.type.Type.ImmutableFileType;

public enum IntrinsicFile {
  TEXT("Text", untypedFile()),
  TEXTFILE("TextFile", untypedFile());

  public final String typeName;
  public final ImmutableFileType type;

  IntrinsicFile(String typeName, ImmutableFileType type) {
    this.typeName = typeName;
    this.type = type;
  }
}
