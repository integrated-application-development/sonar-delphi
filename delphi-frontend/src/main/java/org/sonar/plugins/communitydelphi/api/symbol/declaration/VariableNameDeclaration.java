package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import org.sonar.plugins.communitydelphi.api.ast.Visibility;

public interface VariableNameDeclaration extends TypedDeclaration, Visibility {

  boolean isInline();

  boolean isField();

  boolean isClassVariable();

  boolean isUnion();
}
