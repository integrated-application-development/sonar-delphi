package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.PropertyNameDeclaration;
import javax.annotation.Nullable;

public interface ForInStatementNode extends ForStatementNode {
  @Nullable
  MethodNameDeclaration getGetEnumeratorDeclaration();

  @Nullable
  MethodNameDeclaration getMoveNextDeclaration();

  @Nullable
  PropertyNameDeclaration getCurrentDeclaration();

  ExpressionNode getEnumerable();
}
