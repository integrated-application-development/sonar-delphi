package org.sonar.plugins.delphi.symbol;

public interface Qualifiable {
  default String simpleName() {
    return getQualifiedName().simpleName();
  }

  default String fullyQualifiedName() {
    return getQualifiedName().fullyQualifiedName();
  }

  QualifiedName getQualifiedName();
}
