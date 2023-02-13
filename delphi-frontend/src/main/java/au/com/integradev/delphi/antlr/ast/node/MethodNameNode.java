package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.Qualifiable;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import java.util.List;
import javax.annotation.Nullable;

public interface MethodNameNode extends DelphiNode, Qualifiable {
  NameReferenceNode getNameReferenceNode();

  SimpleNameDeclarationNode getNameDeclarationNode();

  @Nullable
  MethodNameDeclaration getMethodNameDeclaration();

  String simpleNameWithTypeParameters();

  void setMethodNameDeclaration(MethodNameDeclaration declaration);

  List<NameOccurrence> getUsages();
}
