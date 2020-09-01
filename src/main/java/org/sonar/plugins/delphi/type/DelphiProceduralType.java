package org.sonar.plugins.delphi.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public class DelphiProceduralType extends DelphiGenerifiableType implements ProceduralType {
  private final ProceduralKind kind;
  private final ImmutableList<Type> parameterTypes;
  private final Type returnType;

  private DelphiProceduralType(
      ProceduralKind kind, List<? extends Type> parameterTypes, Type returnType) {
    this.kind = kind;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
    this.returnType = returnType;
  }

  public static ProceduralType procedure(List<? extends Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.PROCEDURE, parameterTypes, returnType);
  }

  public static ProceduralType ofObject(List<? extends Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.PROCEDURE_OF_OBJECT, parameterTypes, returnType);
  }

  public static ProceduralType reference(List<? extends Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.REFERENCE, parameterTypes, returnType);
  }

  public static ProceduralType anonymous(List<? extends Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.ANONYMOUS, parameterTypes, returnType);
  }

  public static ProceduralType method(List<? extends Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.METHOD, parameterTypes, returnType);
  }

  @Override
  public String getImage() {
    return kind.name() + makeSignature(parameterTypes, returnType);
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
        kind,
        parameterTypes.stream()
            .map(type -> type.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        returnType.specialize(context));
  }
}
