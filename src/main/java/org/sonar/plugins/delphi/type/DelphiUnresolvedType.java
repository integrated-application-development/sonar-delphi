package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public class DelphiUnresolvedType extends DelphiType implements ImmutableType {
  private final String image;

  private DelphiUnresolvedType(String image) {
    this.image = image;
  }

  public static ImmutableType referenceTo(NameReferenceNode reference) {
    return new DelphiUnresolvedType(reference.getImage());
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public boolean isUnresolved() {
    return true;
  }
}
