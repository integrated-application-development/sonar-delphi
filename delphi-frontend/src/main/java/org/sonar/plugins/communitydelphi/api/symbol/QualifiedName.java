package org.sonar.plugins.communitydelphi.api.symbol;

import com.google.errorprone.annotations.Immutable;
import java.util.List;

@Immutable
public interface QualifiedName {
  String simpleName();

  String fullyQualifiedName();

  List<String> parts();
}
