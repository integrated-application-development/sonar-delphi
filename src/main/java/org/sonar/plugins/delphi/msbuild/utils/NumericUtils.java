package org.sonar.plugins.delphi.msbuild.utils;

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
