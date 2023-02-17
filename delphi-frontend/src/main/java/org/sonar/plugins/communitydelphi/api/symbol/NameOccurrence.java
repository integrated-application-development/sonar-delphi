package org.sonar.plugins.communitydelphi.api.symbol;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

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
