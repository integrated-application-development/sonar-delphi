/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.symbol;

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
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;

@Immutable
@SuppressWarnings("Immutable")
public final class QualifiedNameImpl implements QualifiedName {
  private final ImmutableList<String> parts;
  private final Supplier<String> fullyQualifiedName;

  public QualifiedNameImpl(Collection<String> parts) {
    Preconditions.checkArgument(!parts.isEmpty());
    this.parts = ImmutableList.copyOf(parts);
    this.fullyQualifiedName = Suppliers.memoize(() -> StringUtils.join(parts, "."));
  }

  public static QualifiedName of(String... parts) {
    Preconditions.checkArgument(parts.length > 0);
    return new QualifiedNameImpl(Arrays.asList(parts));
  }

  @Override
  public String simpleName() {
    String result = Iterables.getLast(parts);
    if (result.contains("<")) {
      result = result.substring(0, result.indexOf('<'));
    }
    return result;
  }

  @Override
  public String fullyQualifiedName() {
    return fullyQualifiedName.get();
  }

  @Override
  public List<String> parts() {
    return parts;
  }
}
