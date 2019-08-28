package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiCollectionType;
import org.sonar.plugins.delphi.type.Type;

public final class ArrayTypeNode extends TypeNode {
  public ArrayTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @NotNull
  private TypeNode getElementTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Nullable
  private ArrayIndicesNode getArrayIndices() {
    Node indices = jjtGetChild(2);
    return (indices instanceof ArrayIndicesNode) ? (ArrayIndicesNode) indices : null;
  }

  @Override
  @NotNull
  public Type createType() {
    Node parent = jjtGetParent();
    String image = null;
    if (parent instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) parent).getTypeNameNode().simpleName();
    }
    Type elementType = getElementTypeNode().getType();

    ArrayIndicesNode indices = getArrayIndices();
    int indicesSize = (indices == null) ? 0 : indices.getTypeNodes().size();

    if (indicesSize > 1) {
      return DelphiCollectionType.multiDimensionalArray(image, elementType, indicesSize);
    }

    if (indicesSize > 0) {
      return DelphiCollectionType.fixedArray(image, elementType);
    } else if (parent instanceof FormalParameterNode) {
      return DelphiCollectionType.openArray(image, elementType);
    } else {
      return DelphiCollectionType.dynamicArray(image, elementType);
    }
  }
}
