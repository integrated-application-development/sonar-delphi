package org.sonar.plugins.communitydelphi.api.directive;

import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;

public interface ConditionalDirective extends CompilerDirective {
  enum ConditionalKind {
    ELSEIF,
    ELSE,
    ENDIF,
    IFDEF,
    IF,
    IFEND,
    IFNDEF,
    IFOPT;

    public static Optional<ConditionalKind> find(String name) {
      return Optional.ofNullable(EnumUtils.getEnumIgnoreCase(ConditionalKind.class, name));
    }
  }

  ConditionalKind kind();
}
