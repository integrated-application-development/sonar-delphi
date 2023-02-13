package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import javax.annotation.Nullable;

public interface MethodDeclarationNode extends MethodNode, Visibility {
  boolean isOverride();

  boolean isVirtual();

  boolean isMessage();

  @Nullable
  TypeNameDeclaration getTypeDeclaration();
}
