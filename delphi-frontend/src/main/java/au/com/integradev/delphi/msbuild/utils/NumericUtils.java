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
package au.com.integradev.delphi.msbuild.utils;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class NumericUtils {
  private NumericUtils() {
    // Utility class
  }

  public static Optional<Double> parse(String value) {
    try {
      return Optional.of(Double.parseDouble(value));
    } catch (NumberFormatException e) {
      // do nothing
    }

    if (value.length() > 2 && StringUtils.startsWithIgnoreCase(value, "0x")) {
      try {
        return Optional.of((double) Integer.parseInt(value.substring(2), 16));
      } catch (NumberFormatException e) {
        // do nothing
      }
    }

    return Optional.empty();
  }
}
