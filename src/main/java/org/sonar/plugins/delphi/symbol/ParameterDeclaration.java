package org.sonar.plugins.delphi.symbol;

import java.util.Objects;
import org.assertj.core.util.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class ParameterDeclaration implements Typed {
  private final String image;
  private final Type type;
  private final boolean hasDefaultValue;
  private final boolean isOut;
  private final boolean isVar;
  private final boolean isConst;

  @VisibleForTesting
  public ParameterDeclaration(FormalParameter parameter) {
    image = parameter.getImage();
    type = parameter.getType();
    hasDefaultValue = parameter.hasDefaultValue();
    isOut = parameter.isOut();
    isVar = parameter.isVar();
    isConst = parameter.isConst();
  }

  public String getImage() {
    return image;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }

  public boolean isOut() {
    return isOut;
  }

  public boolean isVar() {
    return isVar;
  }

  public boolean isConst() {
    return isConst;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParameterDeclaration that = (ParameterDeclaration) o;
    return image.equalsIgnoreCase(that.image) && type.is(that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image.toLowerCase(), type.getImage().toLowerCase());
  }
}
