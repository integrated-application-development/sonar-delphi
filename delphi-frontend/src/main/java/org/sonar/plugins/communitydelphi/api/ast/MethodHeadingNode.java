package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface MethodHeadingNode extends DelphiNode {
  MethodNameNode getMethodNameNode();

  MethodParametersNode getMethodParametersNode();

  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();

  MethodReturnTypeNode getMethodReturnType();

  boolean isClassMethod();

  String simpleName();

  String fullyQualifiedName();

  String getTypeName();

  boolean isMethodImplementation();

  boolean isMethodDeclaration();

  MethodKind getMethodKind();

  Set<MethodDirective> getDirectives();
}
