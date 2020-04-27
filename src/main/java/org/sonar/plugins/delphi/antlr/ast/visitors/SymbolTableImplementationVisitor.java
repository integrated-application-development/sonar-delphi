package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.resolve.NameResolver.resolve;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

public class SymbolTableImplementationVisitor extends SymbolTableVisitor {
  @Override
  public Data visit(InterfaceSectionNode node, Data data) {
    return data;
  }

  @Override
  public Data visit(MethodImplementationNode node, Data data) {
    MethodScope methodScope = new MethodScope(node.fullyQualifiedName());
    methodScope.setParent(data.currentScope());
    node.setScope(methodScope);

    resolve(node);

    NameReferenceNode methodReference = node.getNameReferenceNode();
    NameDeclaration declaration = methodReference.getLastName().getNameDeclaration();
    boolean foundInterfaceDeclaration = declaration instanceof MethodNameDeclaration;
    boolean qualifiedMethodName = methodReference.flatten().size() > 1;

    if (!foundInterfaceDeclaration && !qualifiedMethodName) {
      MethodNameDeclaration implementationDeclaration = MethodNameDeclaration.create(node);
      MethodNameNode nameNode = node.getMethodHeading().getMethodNameNode();
      data.addDeclaration(implementationDeclaration, nameNode);
    }

    data.addScope(methodScope, node);

    if (node.isFunction() || node.isOperator()) {
      DelphiNameDeclaration result = compilerVariable("Result", node.getReturnType(), methodScope);
      data.addDeclarationToCurrentScope(result);
    }

    Type selfType = findSelfType(node);
    if (selfType != null) {
      DelphiNameDeclaration self = compilerVariable("Self", selfType, methodScope);
      data.addDeclarationToCurrentScope(self);
    }

    return visitScope(node, data);
  }

  private static Type findSelfType(MethodImplementationNode node) {
    Type selfType = null;
    TypeNameDeclaration methodType = node.getTypeDeclaration();
    if (methodType != null) {
      selfType = methodType.getType();
      if (selfType.isHelper()) {
        selfType = ((HelperType) selfType).extendedType();
      }
    }
    return selfType;
  }
}
