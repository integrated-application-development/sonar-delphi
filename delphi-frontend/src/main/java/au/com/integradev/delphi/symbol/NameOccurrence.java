package au.com.integradev.delphi.symbol;

import au.com.integradev.delphi.antlr.ast.node.Node;
import au.com.integradev.delphi.type.Type;
import java.util.List;

public interface NameOccurrence {

  Node getLocation();

  String getImage();

  NameOccurrence getNameForWhichThisIsAQualifier();

  NameDeclaration getNameDeclaration();

  boolean isPartOfQualifiedName();

  boolean isExplicitInvocation();

  boolean isMethodReference();

  boolean isGeneric();

  List<Type> getTypeArguments();

  boolean isSelf();
}
