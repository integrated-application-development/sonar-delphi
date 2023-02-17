package org.sonar.plugins.communitydelphi.api.type;

import javax.annotation.Nullable;

public interface TypeSpecializationContext {
  @Nullable
  Type getArgument(Type parameter);

  boolean hasSignatureMismatch();
}
