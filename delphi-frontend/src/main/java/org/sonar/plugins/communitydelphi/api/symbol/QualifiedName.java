package org.sonar.plugins.communitydelphi.api.symbol;

import java.util.List;

public interface QualifiedName {
  String simpleName();

  String fullyQualifiedName();

  List<String> parts();
}
