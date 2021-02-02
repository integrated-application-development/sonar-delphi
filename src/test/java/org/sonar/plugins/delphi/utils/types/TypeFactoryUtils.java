package org.sonar.plugins.delphi.utils.types;

import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public final class TypeFactoryUtils {
  private TypeFactoryUtils() {
    // Utility class
  }

  public static TypeFactory defaultFactory() {
    return new TypeFactory(
        DelphiPlugin.COMPILER_TOOLCHAIN_DEFAULT, DelphiPlugin.COMPILER_VERSION_DEFAULT);
  }
}
