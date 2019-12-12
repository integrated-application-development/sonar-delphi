package org.sonar.plugins.delphi.type;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

public class DelphiProceduralType extends DelphiType implements ProceduralType {
  private final ProceduralKind kind;
  private final List<Type> parameterTypes;
  private final Type returnType;

  private DelphiProceduralType(ProceduralKind kind, List<Type> parameterTypes, Type returnType) {
    super(kind.name() + makeSignature(parameterTypes, returnType));
    this.kind = kind;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }

  public static ProceduralType procedure(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.PROCEDURE, parameterTypes, returnType);
  }

  public static ProceduralType ofObject(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.PROCEDURE_OF_OBJECT, parameterTypes, returnType);
  }

  public static ProceduralType reference(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.REFERENCE, parameterTypes, returnType);
  }

  public static ProceduralType anonymous(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.ANONYMOUS, parameterTypes, returnType);
  }

  public static ProceduralType method(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType(ProceduralKind.METHOD, parameterTypes, returnType);
  }

  private static String makeSignature(List<Type> parameterTypes, Type returnType) {
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
}
