package org.sonar.plugins.communitydelphi.api.type;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface TypeSpecializationContext {
  @Nullable
  Type getArgument(Type parameter);

  boolean hasSignatureMismatch();
}
