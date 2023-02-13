package au.com.integradev.delphi.symbol;

import org.sonar.plugins.communitydelphi.api.ast.Node;
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
