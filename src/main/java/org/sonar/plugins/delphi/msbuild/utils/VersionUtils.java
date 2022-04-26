package org.sonar.plugins.delphi.msbuild.utils;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.msbuild.condition.Version;

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
