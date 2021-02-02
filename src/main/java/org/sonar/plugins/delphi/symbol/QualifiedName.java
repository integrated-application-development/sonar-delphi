package org.sonar.plugins.delphi.symbol;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.annotations.Immutable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

@Immutable
@SuppressWarnings("Immutable")
public class QualifiedName {
  private final ImmutableList<String> parts;
  private final Supplier<String> fullyQualifiedName;

  public QualifiedName(Collection<String> parts) {
    Preconditions.checkArgument(!parts.isEmpty());
    this.parts = ImmutableList.copyOf(parts);
    this.fullyQualifiedName = Suppliers.memoize(() -> StringUtils.join(parts, "."));
  }

  public static QualifiedName of(String... parts) {
    Preconditions.checkArgument(parts.length > 0);
    return new QualifiedName(Arrays.asList(parts));
  }

  public String simpleName() {
    String result = Iterables.getLast(parts);
    if (result.contains("<")) {
      result = result.substring(0, result.indexOf('<'));
    }
    return result;
  }

  public String fullyQualifiedName() {
    return fullyQualifiedName.get();
  }

  public List<String> parts() {
    return parts;
  }
}
