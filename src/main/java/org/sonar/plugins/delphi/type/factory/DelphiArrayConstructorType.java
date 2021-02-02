package org.sonar.plugins.delphi.type.factory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;

class DelphiArrayConstructorType extends DelphiType implements ArrayConstructorType {
  private final ImmutableList<Type> elementTypes;

  DelphiArrayConstructorType(List<Type> elementTypes) {
    this.elementTypes = ImmutableList.copyOf(elementTypes);
  }

  @Override
  public String getImage() {
    return "[" + elementTypes.stream().map(Type::getImage).collect(Collectors.joining(",")) + "]";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @Override
  public List<Type> elementTypes() {
    return elementTypes;
  }

  @Override
  public boolean isArrayConstructor() {
    return true;
  }
}
