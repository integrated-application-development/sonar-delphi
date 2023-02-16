package org.sonar.plugins.communitydelphi.api.symbol;

import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

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
