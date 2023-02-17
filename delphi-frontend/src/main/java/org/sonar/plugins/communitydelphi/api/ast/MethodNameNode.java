package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;

public interface MethodNameNode extends DelphiNode, Qualifiable {
  NameReferenceNode getNameReferenceNode();

  SimpleNameDeclarationNode getNameDeclarationNode();

  @Nullable
  MethodNameDeclaration getMethodNameDeclaration();

  String simpleNameWithTypeParameters();

  void setMethodNameDeclaration(MethodNameDeclaration declaration);

  List<NameOccurrence> getUsages();
}
