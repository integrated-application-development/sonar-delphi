/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.utils;

import com.google.common.collect.Iterables;

public final class NameConventionUtils {

  private NameConventionUtils() {
    // Utility class
  }

  private static boolean compliesWithPascalCase(final String name, final String prefix) {
    if (name.length() == prefix.length()) {
      return false;
    }

    char character = name.charAt(prefix.length());
    return Character.isUpperCase(character) || Character.isDigit(character);
  }

  public static boolean compliesWithPascalCase(final String name) {
    return compliesWithPascalCase(name, "");
  }

  public static boolean compliesWithPrefix(final String name, final String prefix) {
    return name.startsWith(prefix) && compliesWithPascalCase(name, prefix);
  }

  public static boolean compliesWithPrefix(final String name, final Iterable<String> prefixes) {
    if (Iterables.isEmpty(prefixes)) {
      return compliesWithPascalCase(name);
    }

    for (final String prefix : prefixes) {
      if (compliesWithPrefix(name, prefix)) {
        return true;
      }
    }

    return false;
  }
}
