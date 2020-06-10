package org.sonar.plugins.delphi.preprocessor.directive;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.TreeMap;

public enum CompilerDirectiveType {
  DEFINE("define"),
  UNDEFINE("undef"),
  IFDEF("ifdef"),
  IFNDEF("ifndef"),
  IFOPT("ifopt"),
  IF("if"),
  ELSE("else"),
  ELSEIF("elseif"),
  ENDIF("endif"),
  IFEND("ifend"),
  INCLUDE("include", "i"),
  SCOPEDENUMS("scopedenums"),
  UNSUPPORTED;

  private final ImmutableSet<String> names;
  private static final Map<String, CompilerDirectiveType> typesByName;

  static {
    typesByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    CompilerDirectiveType[] values = CompilerDirectiveType.values();
    for (CompilerDirectiveType type : values) {
      for (String name : type.names) {
        typesByName.put(name, type);
      }
    }
  }

  CompilerDirectiveType(String... names) {
    this.names = ImmutableSet.copyOf(names);
  }

  /**
   * @param directiveName directive name
   * @return directive type with given name
   */
  public static CompilerDirectiveType getTypeByName(String directiveName) {
    return typesByName.getOrDefault(directiveName, UNSUPPORTED);
  }
}
