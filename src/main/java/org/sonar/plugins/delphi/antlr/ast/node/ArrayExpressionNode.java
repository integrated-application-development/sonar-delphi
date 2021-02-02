package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.Set;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.ArrayOption;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public final class ArrayExpressionNode extends ExpressionNode {
  private String image;

  public ArrayExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<ExpressionNode> getElements() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (ExpressionNode element : getElements()) {
        imageBuilder.append(element.getImage());
      }
      imageBuilder.append(")");
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type elementType = DelphiType.unknownType();
    List<ExpressionNode> elements = getElements();
    if (!elements.isEmpty()) {
      elementType = elements.get(0).getType();
    }
    return getTypeFactory().array(null, elementType, Set.of(ArrayOption.FIXED));
  }
}
