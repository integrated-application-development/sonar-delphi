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

import au.com.integradev.delphi.msbuild.condition.Version;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class VersionUtils {
  private VersionUtils() {
    // Utility class
  }

  public static Optional<Version> parse(String value) {
    String[] parts = StringUtils.split(value, ".");
    if (parts.length >= 2 && parts.length <= 4) {
      try {
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        if (parts.length < 3) {
          return Optional.of(new Version(major, minor));
        }

        int build = Integer.parseInt(parts[2]);
        if (parts.length < 4) {
          return Optional.of(new Version(major, minor, build));
        }

        int revision = Integer.parseInt(parts[3]);
        return Optional.of(new Version(major, minor, build, revision));
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    return Optional.empty();
  }
}
