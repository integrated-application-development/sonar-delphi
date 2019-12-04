package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class FileHeaderNode extends DelphiNode {
  private String namespace;

  FileHeaderNode(Token token) {
    super(token);
  }

  public QualifiedNameDeclarationNode getNameNode() {
    return (QualifiedNameDeclarationNode) jjtGetChild(0);
  }

  public String getName() {
    return getNameNode().fullyQualifiedName();
  }

  public String getNamespace() {
    if (namespace == null) {
      String fullName = getName();
      int dotIndex = fullName.lastIndexOf('.');
      if (dotIndex == -1) {
        namespace = "";
      } else {
        namespace = fullName.substring(0, dotIndex);
      }
    }
    return namespace;
  }
}
