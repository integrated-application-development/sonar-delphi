package org.sonar.plugins.delphi.symbol.scope;

import javax.annotation.Nullable;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class MethodScope extends AbstractDelphiScope {
  private MethodNameDeclaration methodNameDeclaration;
  private DelphiScope typeScope;

  @Nullable
  public DelphiScope getTypeScope() {
    return typeScope;
  }

  public void setTypeScope(DelphiScope typeScope) {
    this.typeScope = typeScope;
  }

  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    return methodNameDeclaration;
  }

  public void setMethodNameDeclaration(MethodNameDeclaration methodNameDeclaration) {
    this.methodNameDeclaration = methodNameDeclaration;
  }

  @Override
  public String toString() {
    String result = "<MethodScope>";
    if (methodNameDeclaration != null) {
      result = methodNameDeclaration.getImage() + " " + result;
    }
    return result;
  }
}
