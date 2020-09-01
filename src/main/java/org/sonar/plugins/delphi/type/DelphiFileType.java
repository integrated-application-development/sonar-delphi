package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public abstract class DelphiFileType extends DelphiType implements FileType {
  public static Type fileOf(Type fileType) {
    return new MutableDelphiFileType(fileType);
  }

  public static ImmutableFileType untypedFile() {
    return new ImmutableDelphiFileType(DelphiType.untypedType());
  }

  @Override
  public String getImage() {
    return "file of " + fileType().getImage();
  }

  @Override
  public boolean isFile() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (fileType().isTypeParameter()) {
      return fileOf(fileType().specialize(context));
    }
    return this;
  }

  private static class MutableDelphiFileType extends DelphiFileType {
    private final Type fileType;

    MutableDelphiFileType(Type fileType) {
      this.fileType = fileType;
    }

    @Override
    public Type fileType() {
      return fileType;
    }
  }

  @Immutable
  private static class ImmutableDelphiFileType extends DelphiFileType implements ImmutableFileType {
    private final ImmutableType fileType;

    ImmutableDelphiFileType(ImmutableType fileType) {
      this.fileType = fileType;
    }

    @Override
    public ImmutableType fileType() {
      return fileType;
    }
  }
}
