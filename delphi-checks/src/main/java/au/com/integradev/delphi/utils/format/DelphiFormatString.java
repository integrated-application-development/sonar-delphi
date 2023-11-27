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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DelphiFormatString {
  private final String image;
  private final List<FormatSpecifier> formatSpecifiers;
  private List<DelphiFormatArgument> arguments;

  public DelphiFormatString(String image, List<FormatSpecifier> formatSpecifiers) {
    this.image = image;
    this.formatSpecifiers = Collections.unmodifiableList(formatSpecifiers);
  }

  public String getImage() {
    return image;
  }

  public List<FormatSpecifier> getFormatSpecifiers() {
    return formatSpecifiers;
  }

  public List<DelphiFormatArgument> getArguments() {
    if (arguments == null) {
      List<Set<FormatSpecifierType>> typesPerArg =
          new FormatArgumentTypeCollector(formatSpecifiers).collect();
      List<Set<FormatSpecifier>> specifiersPerArg =
          new FormatArgumentSpecifierCollector(formatSpecifiers).collect();

      if (typesPerArg.size() != specifiersPerArg.size()) {
        throw new IllegalStateException("Format argument collectors produced different results");
      }

      arguments =
          IntStream.range(0, typesPerArg.size())
              .mapToObj(i -> new DelphiFormatArgument(specifiersPerArg.get(i), typesPerArg.get(i)))
              .collect(Collectors.toList());
    }

    return arguments;
  }
}
