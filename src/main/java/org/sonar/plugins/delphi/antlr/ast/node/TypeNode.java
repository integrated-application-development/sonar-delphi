package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;

public abstract class TypeNode extends DelphiNode {
  private List<QualifiedIdentifierNode> parentTypeNames;

  public TypeNode(Token token) {
    super(token);
  }

  public TypeNode(int tokenType) {
    super(tokenType);
  }

  public ClassParentsNode getParentTypesNode() {
    Node child = jjtGetChild(0);
    return child instanceof ClassParentsNode ? (ClassParentsNode) child : null;
  }

  public final List<QualifiedIdentifierNode> getParentTypeNames() {
    if (parentTypeNames == null) {
      ClassParentsNode parentsNode = getParentTypesNode();
      parentTypeNames =
          parentsNode != null
              ? parentsNode.findChildrenOfType(QualifiedIdentifierNode.class)
              : Collections.emptyList();
    }
    return parentTypeNames;
  }

  @Override
  public abstract String getImage();
}
