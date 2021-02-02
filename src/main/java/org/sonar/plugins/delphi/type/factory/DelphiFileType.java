package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiFileType extends DelphiType implements FileType {
  private final Type fileType;
  private final int size;

  DelphiFileType(Type fileType, int size) {
    this.fileType = fileType;
    this.size = size;
  }

  @Override
  public String getImage() {
    return "file of " + fileType().getImage();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Type fileType() {
    return fileType;
  }

  @Override
  public boolean isFile() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (fileType().isTypeParameter()) {
      return new DelphiFileType(fileType().specialize(context), size);
    }
    return this;
  }
}
