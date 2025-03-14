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

import java.util.Optional;

public interface SwitchDirective extends CompilerDirective {
  enum SwitchKind {
    ALIGN("align", 'a'),
    ALLOWBIND("allowbind"),
    ALLOWISOLATION("allowisolation"),
    ASSERTIONS("assertions", 'c'),
    BOOLEVAL("booleval", 'b'),
    DEBUGINFO("debuginfo", 'd'),
    DENYPACKAGEUNIT("denypackageunit"),
    DESIGNONLY("designonly"),
    OBJEXPORTALL("objexportall"),
    EXTENDEDSYNTAX("extendedsyntax", 'x'),
    EXTENDEDCOMPATIBILITY("extendedcompatibility"),
    EXCESSPRECISION("excessprecision"),
    HIGHENTROPYVA("highentropyva"),
    HIGHCHARUNICODE("highcharunicode"),
    HINTS("hints"),
    IMPLICITBUILD("implicitbuild"),
    IMPORTEDDATA("importeddata", 'g'),
    IOCHECKS("iochecks", 'i'),
    LARGEADDRESSAWARE("largeaddressaware"),
    LEGACYIFEND("legacyifend"),
    LOCALSYMBOLS("localsymbols", 'l'),
    LONGSTRINGS("longstrings", 'h'),
    METHODINFO("methodinfo"),
    NXCOMPAT("nxcompat"),
    OLDTYPELAYOUT("oldtypelayout"),
    OPENSTRINGS("openstrings", 'p'),
    OPTIMIZATION("optimization", 'o'),
    OVERFLOWCHECKS("overflowchecks", 'q'),
    SAFEDIVIDE("safedivide", 'u'),
    POINTERMATH("pointermath"),
    RANGECHECKS("rangechecks", 'r'),
    REALCOMPATIBILITY("realcompatibility"),
    RUNONLY("runonly"),
    TSAWARE("tsaware"),
    TYPEINFO("typeinfo", 'm'),
    SCOPEDENUMS("scopedenums"),
    STACKFRAMES("stackframes", 'w'),
    STRONGLINKTYPES("stronglinktypes"),
    DEFINITIONINFO("definitioninfo"),
    REFERENCEINFO("referenceinfo", 'y'),
    /**
     * @deprecated Use {@link SwitchKind#TYPEDADDRESS} instead.
     */
    @Deprecated(forRemoval = true)
    TYPEADDRESS(null, null),
    TYPEDADDRESS("typedaddress", 't'),
    VARSTRINGCHECKS("varstringchecks", 'v'),
    WARNINGS("warnings"),
    WEAKPACKAGEUNIT("weakpackageunit"),
    WEAKLINKRTTI("weaklinkrtti"),
    WRITEABLECONST("writeableconst", 'j'),
    ZEROBASEDSTRINGS("zerobasedstrings"),
    MINENUMSIZE("z"),
    MINENUMSIZE1("z1"),
    MINENUMSIZE2("z2"),
    MINENUMSIZE4("z4");

    private final String name;
    private final String shortName;

    SwitchKind(String name) {
      this(name, null);
    }

    SwitchKind(String name, Character shortName) {
      this.name = name;
      this.shortName = (shortName == null) ? null : shortName.toString();
    }

    public static Optional<SwitchKind> find(String name) {
      for (SwitchKind kind : SwitchKind.values()) {
        if (name.equalsIgnoreCase(kind.name) || name.equalsIgnoreCase(kind.shortName)) {
          return Optional.of(kind);
        }
      }

      if (name.equalsIgnoreCase("yd")) {
        // This is a super weird outlier for a "short name", in the sense that:
        // - It is not a single character
        // - It does not use '+' or '-' to indicate the value of the switch
        // - {$YD} is interpreted as ${DEFINITIONINFO ON} and cannot represent the "OFF" value
        return Optional.of(DEFINITIONINFO);
      }

      return Optional.empty();
    }
  }

  SwitchKind kind();

  boolean isActive();
}
