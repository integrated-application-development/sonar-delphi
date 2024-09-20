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
package au.com.integradev.delphi.msbuild.condition;

import au.com.integradev.delphi.msbuild.utils.VersionUtils;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class VersionTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(version("1.0"))
        .addEqualityGroup(version("1.0.0"))
        .addEqualityGroup(version("1.0.0.0"))
        .addEqualityGroup(version("1.1.0.0"))
        .addEqualityGroup(version("1.1.1.0"))
        .addEqualityGroup(version("1.1.1.1"))
        .addEqualityGroup(version("2.0.0.0"))
        .testEquals();
  }

  private static Version version(String input) {
    return VersionUtils.parse(input).orElseThrow(AssertionError::new);
  }
}
