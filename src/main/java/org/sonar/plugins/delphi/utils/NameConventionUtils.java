package org.sonar.plugins.delphi.utils;

public class NameConventionUtils {

  private NameConventionUtils() {
    // Utility class
  }

  public static boolean compliesWithPrefix(final String name, final String prefix) {
    return name.startsWith(prefix) && compliesWithPascalCase(name, prefix);
  }

  public static boolean compliesWithPrefix(final String name, final String[] prefixes) {
    for (final String prefix : prefixes) {
      if (compliesWithPrefix(name, prefix)) {
        return true;
      }
    }

    return false;
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
}
