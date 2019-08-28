package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;

public class DelphiUnresolvedType extends DelphiType {
  private DelphiUnresolvedType(NameReferenceNode reference) {
    super(reference.getImage());
  }

  public static Type referenceTo(NameReferenceNode reference) {
    return new DelphiUnresolvedType(reference);
  }

  @Override
  public boolean isUnresolved() {
    return true;
  }
}
