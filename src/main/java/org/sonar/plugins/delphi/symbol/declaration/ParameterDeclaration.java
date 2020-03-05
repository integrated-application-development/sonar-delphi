package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import org.assertj.core.util.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethodData.IntrinsicMethodParameterData;

public final class ParameterDeclaration implements Typed, Comparable<ParameterDeclaration> {
  private final String image;
  private final Type type;
  private final boolean hasDefaultValue;
  private final boolean isOut;
  private final boolean isVar;
  private final boolean isConst;

  @VisibleForTesting
  private ParameterDeclaration(
      String image,
      Type type,
      boolean hasDefaultValue,
      boolean isOut,
      boolean isVar,
      boolean isConst) {
    this.image = image;
    this.type = type;
    this.hasDefaultValue = hasDefaultValue;
    this.isOut = isOut;
    this.isVar = isVar;
    this.isConst = isConst;
  }

  public static ParameterDeclaration create(FormalParameter parameter) {
    return new ParameterDeclaration(
        parameter.getImage(),
        parameter.getType(),
        parameter.hasDefaultValue(),
        parameter.isOut(),
        parameter.isVar(),
        parameter.isConst());
  }

  public static ParameterDeclaration create(IntrinsicMethodParameterData data) {
    return new ParameterDeclaration(
        "_", data.getType(), data.hasDefaultValue(), false, false, false);
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

  public ParameterDeclaration specialize(TypeSpecializationContext context) {
    ParameterDeclaration specialized =
        new ParameterDeclaration(
            image, type.specialize(context), hasDefaultValue, isOut, isVar, isConst);
    if (this.equals(specialized)) {
      return this;
    }
    return specialized;
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
    return Objects.hash(image.toLowerCase(), type.getImage());
  }

  @Override
  public int compareTo(@NotNull ParameterDeclaration other) {
    return ComparisonChain.start()
        .compare(image, other.image, String.CASE_INSENSITIVE_ORDER)
        .compare(type.getImage(), other.type.getImage())
        .result();
  }
}
