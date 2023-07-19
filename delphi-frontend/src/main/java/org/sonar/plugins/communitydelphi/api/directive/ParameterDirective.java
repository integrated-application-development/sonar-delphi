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
    WARN("warn");

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
