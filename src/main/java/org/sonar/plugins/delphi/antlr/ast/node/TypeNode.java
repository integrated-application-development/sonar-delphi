package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public abstract class TypeNode extends DelphiNode implements Typed {
  private Type type;
  private List<TypeReferenceNode> parentTypeNodes;
  private Set<Type> parentTypes;

  public TypeNode(Token token) {
    super(token);
  }

  public TypeNode(int tokenType) {
    super(tokenType);
  }

  private AncestorListNode getAncestorListNode() {
    Node child = jjtGetChild(0);
    return child instanceof AncestorListNode ? (AncestorListNode) child : null;
  }

  public final List<TypeReferenceNode> getParentTypeNodes() {
    if (parentTypeNodes == null) {
      AncestorListNode parentsNode = getAncestorListNode();
      parentTypeNodes =
          parentsNode != null
              ? parentsNode.findChildrenOfType(TypeReferenceNode.class)
              : Collections.emptyList();
    }
    return parentTypeNodes;
  }

  public final Set<Type> getParentTypes() {
    if (parentTypes == null) {
      parentTypes =
          getParentTypeNodes().stream()
              .map(TypeReferenceNode::getType)
              .collect(Collectors.toUnmodifiableSet());
    }
    return parentTypes;
  }

  @Override
  public final String getImage() {
    return getType().getImage();
  }

  @Override
  @NotNull
  public final Type getType() {
    if (type == null) {
      type = createType();
    }
    return type;
  }

  @NotNull
  protected abstract Type createType();
}
