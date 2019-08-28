package org.sonar.plugins.delphi.symbol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class QualifiedName implements Iterable<String> {
  private final Collection<String> names;
  private String simpleName;
  private String fullyQualifiedName;

  public QualifiedName(Collection<String> names) {
    Preconditions.checkArgument(!names.isEmpty());
    this.names = names;
  }

  public String simpleName() {
    if (simpleName == null) {
      simpleName = Iterables.getLast(names);
    }
    return simpleName;
  }

  public String fullyQualifiedName() {
    if (fullyQualifiedName == null) {
      fullyQualifiedName = StringUtils.join(names, ".");
    }
    return fullyQualifiedName;
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return names.iterator();
  }

  @Override
  public String toString() {
    return fullyQualifiedName();
  }
}
