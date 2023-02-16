package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;

public interface ForInStatementNode extends ForStatementNode {
  @Nullable
  MethodNameDeclaration getGetEnumeratorDeclaration();

  @Nullable
  MethodNameDeclaration getMoveNextDeclaration();

  @Nullable
  PropertyNameDeclaration getCurrentDeclaration();

  ExpressionNode getEnumerable();
}
