package org.sonar.plugins.delphi.symbol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class QualifiedName {
  private final List<String> parts;
  private String simpleName;
  private String fullyQualifiedName;

  public QualifiedName(Collection<String> parts) {
    Preconditions.checkArgument(!parts.isEmpty());
    this.parts = List.copyOf(parts);
  }

  public static QualifiedName of(String... parts) {
    Preconditions.checkArgument(parts.length > 0);
    return new QualifiedName(Arrays.asList(parts));
  }

  public String simpleName() {
    if (simpleName == null) {
      simpleName = Iterables.getLast(parts);
    }
    return simpleName;
  }

  public String fullyQualifiedName() {
    if (fullyQualifiedName == null) {
      fullyQualifiedName = StringUtils.join(parts, ".");
    }
    return fullyQualifiedName;
  }

  public List<String> parts() {
    return parts;
  }
}
