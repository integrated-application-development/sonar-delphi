/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.directive;

import au.com.integradev.delphi.compiler.Platform;
import java.util.Optional;

public interface ParameterDirective extends CompilerDirective {
  enum ParameterKind {
    APPTYPE("apptype"),
    CODEALIGN("codealign"),
    DEFINE("define"),
    DESCRIPTION("description"),
    EXTENSION("extension", 'e'),
    EXTERNALSYM("externalsym"),
    HPPEMIT("hppemit"),
    IMAGEBASE("imagebase"),
    INCLUDE("include", 'i'),
    LIBPREFIX("libprefix"),
    LIBSUFFIX("libsuffix"),
    LIBVERSION("libversion"),
    LINK("link", 'l'),
    STACKSIZE(null, 'm', Platform.WINDOWS),
    MINSTACKSIZE("minstacksize"),
    MAXPAGESIZE("maxpagesize"),
    MAXSTACKSIZE("maxstacksize"),
    MESSAGE("message"),
    MINENUMSIZE("minenumsize"),
    NODEFINE("nodefine"),
    NOINCLUDE("noinclude"),
    OBJTYPENAME("objtypename"),
    SETPEFLAGS("setpeflags"),
    SETPEOPTFLAGS("setpeoptflags"),
    SETPEOSVERSION("setpeosversion"),
    SETPESUBSYSVERSION("setpesubsysversion"),
    SETPEUSERVERSION("setpeuserversion"),
    REGION("region"),
    ENDREGION("endregion"),
    RESOURCERESERVE("resourcereserve", 'm', Platform.LINUX),
    RESOURCE("resource", 'r'),
    RTTI("rtti"),
    UNDEF("undef"),
    WARN("warn"),
    TEXTBLOCK("textblock");

    private final String name;
    private final String shortName;
    private final Platform platform;

    ParameterKind(String name) {
      this(name, null, null);
    }

    ParameterKind(String name, Character shortName) {
      this(name, shortName, null);
    }

    ParameterKind(String name, Character shortName, Platform platform) {
      this.name = name;
      this.shortName = (shortName == null) ? null : shortName.toString();
      this.platform = platform;
    }

    public static Optional<ParameterKind> find(String name, Platform platform) {
      for (ParameterKind kind : ParameterKind.values()) {
        if (kind.platform != null && kind.platform != platform) {
          continue;
        }
        if (name.equalsIgnoreCase(kind.name) || name.equalsIgnoreCase(kind.shortName)) {
          return Optional.of(kind);
        }
      }
      return Optional.empty();
    }
  }

  ParameterKind kind();
}
