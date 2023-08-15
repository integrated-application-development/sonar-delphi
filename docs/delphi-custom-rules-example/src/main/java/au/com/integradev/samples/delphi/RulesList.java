/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import au.com.integradev.samples.delphi.checks.ExtendingTFooCheck;
import au.com.integradev.samples.delphi.checks.MethodDeclarationCheck;
import au.com.integradev.samples.delphi.checks.StringInMethodNameCheck;
import java.util.List;

public final class RulesList {
  private static final List<Class<?>> ALL_CHECKS =
      List.of(
          // Listed alphabetically
          ExtendingTFooCheck.class, MethodDeclarationCheck.class, StringInMethodNameCheck.class);

  private RulesList() {
    // Utility class
  }

  public static List<Class<?>> getChecks() {
    return ALL_CHECKS;
  }
}
