package org.sonar.plugins.delphi.symbol.declaration;

import java.util.List;

public interface GenerifiableDeclaration extends DelphiNameDeclaration {
  List<TypedDeclaration> getTypeParameters();

  default boolean isGeneric() {
    return !getTypeParameters().isEmpty();
  }
}
