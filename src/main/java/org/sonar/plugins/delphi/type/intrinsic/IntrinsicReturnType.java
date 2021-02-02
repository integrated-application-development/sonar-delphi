package org.sonar.plugins.delphi.type.intrinsic;

import java.util.List;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public abstract class IntrinsicReturnType extends DelphiType {
  @Override
  public String getImage() {
    return "<" + getClass().getSimpleName() + ">";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  public abstract Type getReturnType(List<Type> arguments);

  public static Type high(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type low(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type classReferenceValue() {
    return new ClassReferenceValueType();
  }

  private static class HighLowReturnType extends IntrinsicReturnType {
    private final Type integerType;

    private HighLowReturnType(TypeFactory typeFactory) {
      this.integerType = typeFactory.getIntrinsic(IntrinsicType.INTEGER);
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);

      if (type.isClassReference()) {
        type = ((ClassReferenceType) type).classType();
      }

      if (type.isArray() || type.isString()) {
        type = integerType;
      }

      return type;
    }
  }

  private static class ClassReferenceValueType extends IntrinsicReturnType {
    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);
      if (type.isClassReference()) {
        return ((ClassReferenceType) type).classType();
      }
      return unknownType();
    }
  }
}
