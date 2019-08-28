package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.type.Type.FileType;

public class DelphiFileType extends DelphiType implements FileType {
  private Type fileType;

  private DelphiFileType(Type fileType) {
    super("file of " + fileType.getImage());
    this.fileType = fileType;
  }

  public static Type fileOf(Type fileType) {
    return new DelphiFileType(fileType);
  }

  public static Type untypedFile() {
    return new DelphiFileType(DelphiType.untypedType());
  }

  @Override
  public Type fileType() {
    return fileType;
  }

  @Override
  public boolean isFile() {
    return true;
  }
}
