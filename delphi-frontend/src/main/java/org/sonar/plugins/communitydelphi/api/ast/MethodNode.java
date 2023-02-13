package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.symbol.declaration.MethodDirective;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.type.Type;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

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
