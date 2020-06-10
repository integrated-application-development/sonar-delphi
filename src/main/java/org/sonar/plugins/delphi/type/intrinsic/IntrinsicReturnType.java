package org.sonar.plugins.delphi.type.intrinsic;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;

import com.google.errorprone.annotations.Immutable;
import java.util.List;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public final class IntrinsicReturnType extends DelphiType implements ImmutableType {
  public static final IntrinsicReturnType LOW_RETURN_TYPE =
      new IntrinsicReturnType("<low>", IntrinsicReturnType::highLowReturnType);

  public static final IntrinsicReturnType HIGH_RETURN_TYPE =
      new IntrinsicReturnType("<high>", IntrinsicReturnType::highLowReturnType);

  @Immutable
  @FunctionalInterface
  private interface ReturnTypeFunction {
    Type getReturnType(List<Type> arguments);
  }

  private final ReturnTypeFunction function;

  private IntrinsicReturnType(String image, ReturnTypeFunction function) {
    super(image);
    this.function = function;
  }

  public Type getReturnType(List<Type> arguments) {
    return function.getReturnType(arguments);
  }

  private static Type highLowReturnType(List<Type> arguments) {
    Type type = arguments.get(0);

    if (type.isClassReference()) {
      type = ((ClassReferenceType) type).classType();
    }

    if (type.isArray() || type.isString()) {
      type = INTEGER.type;
    }

    return type;
  }
}
