package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public class DelphiUnresolvedType extends DelphiType implements ImmutableType {
  private DelphiUnresolvedType(NameReferenceNode reference) {
    super(reference.getImage());
  }

  public static ImmutableType referenceTo(NameReferenceNode reference) {
    return new DelphiUnresolvedType(reference);
  }

  @Override
  public boolean isUnresolved() {
    return true;
  }
}
