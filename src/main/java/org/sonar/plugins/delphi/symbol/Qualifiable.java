package org.sonar.plugins.delphi.symbol;

import java.util.List;

public interface Qualifiable {
  default String simpleName() {
    return getQualifiedName().simpleName();
  }

  default String fullyQualifiedName() {
    return getQualifiedName().fullyQualifiedName();
  }

  default List<String> getQualifiedNameParts() {
    return getQualifiedName().parts();
  }

  default boolean isQualified() {
    return getQualifiedNameParts().size() > 1;
  }

  QualifiedName getQualifiedName();
}
