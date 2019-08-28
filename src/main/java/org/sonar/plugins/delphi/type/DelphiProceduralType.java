package org.sonar.plugins.delphi.type;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

public class DelphiProceduralType extends DelphiType implements ProceduralType {
  private final List<Type> parameterTypes;
  private final Type returnType;

  protected DelphiProceduralType(String name, List<Type> parameterTypes, Type returnType) {
    super(name + makeSignature(parameterTypes, returnType));
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }

  public static ProceduralType anonymous(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType("anonymous", parameterTypes, returnType);
  }

  public static ProceduralType procedure(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType("procedure", parameterTypes, returnType);
  }

  public static ProceduralType reference(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType("reference", parameterTypes, returnType);
  }

  public static ProceduralType ofObject(List<Type> parameterTypes, Type returnType) {
    return new DelphiProceduralType("procedure of object", parameterTypes, returnType);
  }

  private static String makeSignature(List<Type> parameterTypes, Type returnType) {
    return "("
        + parameterTypes.stream().map(Type::getImage).collect(Collectors.joining(", "))
        + "): "
        + returnType.getImage();
  }

  @Override
  public boolean isProcedural() {
    return true;
  }

  @Override
  public List<Type> parameterTypes() {
    return parameterTypes;
  }

  @Override
  public Type returnType() {
    return returnType;
  }
}
