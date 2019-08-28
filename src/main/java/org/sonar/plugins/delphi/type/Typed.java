package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;

public interface Typed {
  @NotNull
  Type getType();
}
