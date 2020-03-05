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
  private QualifiedName qualifiedName;
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

  public SimpleNameDeclarationNode getNameDeclarationNode() {
    return getFirstChildOfType(SimpleNameDeclarationNode.class);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      NameReferenceNode nameReference = getNameReferenceNode();
      if (nameReference != null) {
        qualifiedName = nameReference.getQualifiedName();
      } else {
        qualifiedName = QualifiedName.of(getNameDeclarationNode().getImage());
      }
    }
    return qualifiedName;
  }

  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    if (methodNameDeclaration == null) {
      methodNameDeclaration = findMethodNameDeclaration();
    }

    return methodNameDeclaration;
  }

  public String simpleNameWithTypeParameters() {
    NameDeclarationNode declarationNode = getNameDeclarationNode();
    NameReferenceNode referenceNode = getNameReferenceNode();
    String typeParameters = "";

    if (declarationNode != null) {
      GenericDefinitionNode genericDefinition = declarationNode.getGenericDefinition();
      if (genericDefinition != null) {
        typeParameters = genericDefinition.getImage();
      }
    } else if (referenceNode != null) {
      GenericArgumentsNode genericArguments = referenceNode.getLastName().getGenericArguments();
      if (genericArguments != null) {
        typeParameters = genericArguments.getImage();
      }
    }

    return simpleName() + typeParameters;
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
        usages = methodNameDeclaration.getScope().getOccurrencesFor(methodNameDeclaration);
      } else {
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
