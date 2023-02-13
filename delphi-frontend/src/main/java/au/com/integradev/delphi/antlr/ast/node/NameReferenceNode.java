package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.Qualifiable;
import au.com.integradev.delphi.type.Typed;
import java.util.List;

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
