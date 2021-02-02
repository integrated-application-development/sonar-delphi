package org.sonar.plugins.delphi.type.factory;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiProceduralType extends DelphiGenerifiableType implements ProceduralType {
  private final int size;
  private final ProceduralKind kind;
  private final List<Type> parameterTypes;
  private final Type returnType;

  DelphiProceduralType(int size, ProceduralKind kind, List<Type> parameterTypes, Type returnType) {
    this.size = size;
    this.kind = kind;
    this.parameterTypes = List.copyOf(parameterTypes);
    this.returnType = returnType;
  }

  @Override
  public String getImage() {
    return kind.name() + makeSignature(parameterTypes, returnType);
  }

  @Override
  public int size() {
    return size;
  }

  private static String makeSignature(List<? extends Type> parameterTypes, Type returnType) {
    return "("
        + parameterTypes.stream().map(Type::getImage).collect(Collectors.joining(", "))
        + "): "
        + returnType.getImage();
  }

  @Override
  public List<Type> parameterTypes() {
    return parameterTypes;
  }

  @Override
  public Type returnType() {
    return returnType;
  }

  @Override
  public ProceduralKind kind() {
    return kind;
  }

  @Override
  public boolean isProcedural() {
    return true;
  }

  @Override
  public boolean isMethod() {
    return kind == ProceduralKind.METHOD;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    if (returnType.canBeSpecialized(context)) {
      return true;
    }
    return parameterTypes.stream().anyMatch(type -> type.canBeSpecialized(context));
  }

  @Override
  public DelphiGenerifiableType doSpecialization(TypeSpecializationContext context) {
    return new DelphiProceduralType(
        size,
        kind,
        parameterTypes.stream()
            .map(type -> type.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        returnType.specialize(context));
  }
}
