package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface MethodNode extends DelphiNode, Visibility {
  MethodHeadingNode getMethodHeading();

  MethodNameNode getMethodNameNode();

  String simpleName();

  String fullyQualifiedName();

  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();

  Type getReturnType();

  MethodKind getMethodKind();

  Set<MethodDirective> getDirectives();

  boolean hasDirective(MethodDirective directive);

  boolean isConstructor();

  boolean isDestructor();

  boolean isFunction();

  boolean isOperator();

  boolean isProcedure();

  boolean isClassMethod();

  String getTypeName();

  @Nullable
  MethodNameDeclaration getMethodNameDeclaration();

  @Nullable
  TypeNameDeclaration getTypeDeclaration();
}
