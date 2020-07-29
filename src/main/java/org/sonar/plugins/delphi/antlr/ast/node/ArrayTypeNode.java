package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.EnumSet;
import java.util.Set;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiArrayType;
import org.sonar.plugins.delphi.type.DelphiArrayType.ArrayOption;
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
      image = ((TypeDeclarationNode) parent).fullyQualifiedName();
    }

    Type elementType = getElementTypeNode().getType();
    ArrayIndicesNode indices = getArrayIndices();
    int indicesSize = (indices == null) ? 0 : indices.getTypeNodes().size();

    Set<ArrayOption> options = EnumSet.noneOf(ArrayOption.class);
    if (indicesSize > 0) {
      options.add(ArrayOption.FIXED);
    } else if (parent instanceof FormalParameterNode) {
      options.add(ArrayOption.OPEN);
    } else {
      options.add(ArrayOption.DYNAMIC);
    }

    if (getElementTypeNode() instanceof ConstArraySubTypeNode) {
      options.add(ArrayOption.ARRAY_OF_CONST);
    }

    if (indicesSize > 1) {
      return DelphiArrayType.multiDimensionalArray(image, elementType, indicesSize, options);
    }

    return DelphiArrayType.array(image, elementType, options);
  }
}
