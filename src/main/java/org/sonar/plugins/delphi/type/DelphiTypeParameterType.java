package org.sonar.plugins.delphi.type;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;

public class DelphiTypeParameterType extends DelphiType implements TypeParameterType {
  private List<Type> constraints;

  private DelphiTypeParameterType(String image, List<Type> constraints) {
    super(image);
    this.constraints = ImmutableList.copyOf(constraints);
  }

  public static TypeParameterType create(String image, List<Type> constraints) {
    return new DelphiTypeParameterType(image, constraints);
  }

  public static TypeParameterType create(String image) {
    return create(image, Collections.emptyList());
  }

  @Override
  public List<Type> constraints() {
    return constraints;
  }

  @Override
  public boolean isTypeParameter() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    Type type = context.getArgument(this);
    return Objects.requireNonNullElse(type, this);
  }

  @Override
  public void setFullType(TypeParameterType fullType) {
    this.constraints = fullType.constraints();
  }
}
