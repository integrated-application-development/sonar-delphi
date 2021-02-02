package org.sonar.plugins.delphi.type.generic;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;

public class DelphiTypeParameterType extends DelphiType implements TypeParameterType {
  private final String image;
  private List<Type> constraints;

  private DelphiTypeParameterType(String image, List<Type> constraints) {
    this.image = image;
    this.constraints = ImmutableList.copyOf(constraints);
  }

  public static TypeParameterType create(String image, List<Type> constraints) {
    return new DelphiTypeParameterType(image, constraints);
  }

  public static TypeParameterType create(String image) {
    return create(image, Collections.emptyList());
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // meta type
    return 0;
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
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    return context.getArgument(this) != null;
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
