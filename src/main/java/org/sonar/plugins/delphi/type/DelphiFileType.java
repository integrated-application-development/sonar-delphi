package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.FileType;

public abstract class DelphiFileType extends DelphiType implements FileType {
  protected DelphiFileType(String image) {
    super("file of " + image);
  }

  public static Type fileOf(Type fileType) {
    return new MutableDelphiFileType(fileType);
  }

  public static ImmutableFileType untypedFile() {
    return new ImmutableDelphiFileType(DelphiType.untypedType());
  }

  @Override
  public boolean isFile() {
    return true;
  }

  private static class MutableDelphiFileType extends DelphiFileType {
    private final Type fileType;

    MutableDelphiFileType(Type fileType) {
      super(fileType.getImage());
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
      super(fileType.getImage());
      this.fileType = fileType;
    }

    @Override
    public ImmutableType fileType() {
      return fileType;
    }
  }
}
