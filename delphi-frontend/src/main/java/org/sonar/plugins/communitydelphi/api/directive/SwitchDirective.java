package org.sonar.plugins.communitydelphi.api.directive;

import java.util.Optional;

public interface SwitchDirective extends CompilerDirective {
  enum SwitchKind {
    ALIGN("align", 'a'),
    ASSERTIONS("assertions", 'c'),
    BOOLEVAL("booleval", 'b'),
    DEBUGINFO("debuginfo", 'd'),
    DENYPACKAGEUNIT("denypackageunit"),
    DESIGNONLY("designonly"),
    OBJEXPORTALL("objexportall"),
    EXTENDEDSYNTAX("extendedsyntax", 'x'),
    EXTENDEDCOMPATIBILITY("extendedcompatibility"),
    EXCESSPRECISION("excessprecision"),
    HINTS("hints"),
    IMPLICITBUILD("implicitbuild"),
    IMPORTEDDATA("importeddata", 'g'),
    IOCHECKS("iochecks", 'i'),
    LEGACYIFEND("legacyifend"),
    LOCALSYMBOLS("localsymbols", 'l'),
    LONGSTRINGS("longstrings", 'h'),
    METHODINFO("methodinfo"),
    OLDTYPELAYOUT("oldtypelayout"),
    OPENSTRINGS("openstrings", 'p'),
    OPTIMIZATION("optimization", 'o'),
    OVERFLOWCHECKS("overflowchecks", 'q'),
    SAFEDIVIDE("safedivide", 'u'),
    POINTERMATH("pointermath"),
    RANGECHECKS("rangechecks", 'r'),
    REALCOMPATIBILITY("realcompatibility"),
    RUNONLY("runonly"),
    TYPEINFO("typeinfo", 'm'),
    SCOPEDENUMS("scopedenums"),
    STACKFRAMES("stackframes", 'w'),
    STRONGLINKTYPES("stronglinktypes"),
    DEFINITIONINFO("definitioninfo"),
    REFERENCEINFO("referenceinfo", 'y'),
    TYPEADDRESS("typeaddress", 't'),
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
