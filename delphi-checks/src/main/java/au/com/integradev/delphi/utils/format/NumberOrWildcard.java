/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.utils.format;

import java.util.Objects;
import java.util.Optional;

public final class NumberOrWildcard {
  private final int value;
  private final boolean isWildcard;

  private NumberOrWildcard(int value, boolean isWildcard) {
    this.value = value;
    this.isWildcard = isWildcard;
  }

  public static NumberOrWildcard number(int value) {
    return new NumberOrWildcard(value, false);
  }

  public static NumberOrWildcard wildcard() {
    return new NumberOrWildcard(0, true);
  }

  public Optional<Integer> getValue() {
    return isWildcard ? Optional.empty() : Optional.of(value);
  }

  public boolean isWildcard() {
    return isWildcard;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NumberOrWildcard)) {
      return false;
    }

    NumberOrWildcard that = (NumberOrWildcard) o;
    return (isWildcard && that.isWildcard) || (value == that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, isWildcard);
  }
}
