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

import java.util.Optional;

public class FormatSpecifier {
  private final Integer index;
  private final boolean leftJustified;
  private final NumberOrWildcard width;
  private final NumberOrWildcard precision;
  private final FormatSpecifierType type;

  public FormatSpecifier(
      FormatSpecifierType type,
      Integer index,
      boolean leftJustified,
      NumberOrWildcard width,
      NumberOrWildcard precision) {
    this.type = type;
    this.index = index;
    this.leftJustified = leftJustified;
    this.width = width;
    this.precision = precision;
  }

  public Optional<Integer> getIndex() {
    return Optional.ofNullable(index);
  }

  public boolean isLeftJustified() {
    return leftJustified;
  }

  public Optional<NumberOrWildcard> getWidth() {
    return Optional.ofNullable(width);
  }

  public Optional<NumberOrWildcard> getPrecision() {
    return Optional.ofNullable(precision);
  }

  public FormatSpecifierType getType() {
    return type;
  }
}
