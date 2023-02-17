package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface NameReferenceNode extends DelphiNode, Qualifiable, Typed {
  List<NameReferenceNode> flatten();

  IdentifierNode getIdentifier();

  GenericArgumentsNode getGenericArguments();

  NameReferenceNode prevName();

  NameReferenceNode nextName();

  NameOccurrence getNameOccurrence();

  NameDeclaration getNameDeclaration();

  NameReferenceNode getLastName();

  boolean isExplicitArrayConstructorInvocation();
}
