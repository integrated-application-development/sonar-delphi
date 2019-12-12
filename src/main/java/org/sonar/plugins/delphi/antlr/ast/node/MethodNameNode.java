package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public final class MethodNameNode extends DelphiNode implements Qualifiable {
  private MethodNameDeclaration methodNameDeclaration;
  private List<NameOccurrence> usages;

  public MethodNameNode(Token token) {
    super(token);
  }

  public MethodNameNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameReferenceNode getNameReferenceNode() {
    return getFirstChildOfType(NameReferenceNode.class);
  }

  public QualifiedNameDeclarationNode getNameDeclarationNode() {
    return getFirstChildOfType(QualifiedNameDeclarationNode.class);
  }

  @Override
  public QualifiedName getQualifiedName() {
    return ((Qualifiable) jjtGetChild(0)).getQualifiedName();
  }

  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    if (methodNameDeclaration == null) {
      methodNameDeclaration = findMethodNameDeclaration();
    }

    return methodNameDeclaration;
  }

  @Override
  public String getImage() {
    return simpleName();
  }

  public void setMethodNameDeclaration(MethodNameDeclaration declaration) {
    this.methodNameDeclaration = declaration;
  }

  public List<NameOccurrence> getUsages() {
    if (usages == null) {
      if (methodNameDeclaration != null) {
        usages =
            methodNameDeclaration
                .getScope()
                .getDeclarations(MethodNameDeclaration.class)
                .get(methodNameDeclaration);
      }
      if (usages == null) {
        usages = Collections.emptyList();
      }
    }
    return usages;
  }

  private MethodNameDeclaration findMethodNameDeclaration() {
    // Interface method declaration
    NameDeclarationNode declarationNode = getNameDeclarationNode();
    if (declarationNode != null) {
      DelphiNameDeclaration declaration = declarationNode.getNameDeclaration();
      if (declaration instanceof MethodNameDeclaration) {
        return (MethodNameDeclaration) declaration;
      }
    }

    // Implementation method referring back to interface declaration
    NameReferenceNode referenceNode = getNameReferenceNode();
    if (referenceNode != null) {
      for (NameReferenceNode name : referenceNode.flatten()) {
        DelphiNameDeclaration declaration = name.getNameDeclaration();
        if (declaration instanceof MethodNameDeclaration) {
          return (MethodNameDeclaration) declaration;
        }
      }
    }

    return null;
  }
}
