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

import java.util.Collections;
import java.util.Set;

public class DelphiFormatArgument {
  private final Set<FormatSpecifier> specifiers;
  private final Set<FormatSpecifierType> types;

  public DelphiFormatArgument(Set<FormatSpecifier> specifiers, Set<FormatSpecifierType> types) {
    this.specifiers = Collections.unmodifiableSet(specifiers);
    this.types = Collections.unmodifiableSet(types);
  }

  public Set<FormatSpecifierType> getTypes() {
    return types;
  }

  public Set<FormatSpecifier> getSpecifiers() {
    return specifiers;
  }
}
