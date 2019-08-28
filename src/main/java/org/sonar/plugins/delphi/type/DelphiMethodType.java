package org.sonar.plugins.delphi.type;

import java.util.List;

public class DelphiMethodType extends DelphiProceduralType {
  private DelphiMethodType(String name, List<Type> parameterTypes, Type returnType) {
    super(name, parameterTypes, returnType);
  }

  public static ProceduralType method(List<Type> parameterTypes, Type returnType) {
    return new DelphiMethodType("method", parameterTypes, returnType);
  }

  @Override
  public boolean isMethod() {
    return true;
  }
}
