package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.symbol.declaration.MethodDirective;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.type.Type;
import java.util.List;
import java.util.Set;

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
