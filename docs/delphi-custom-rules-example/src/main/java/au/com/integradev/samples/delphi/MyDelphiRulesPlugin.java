/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import org.sonar.api.Plugin;

public class MyDelphiRulesPlugin implements Plugin {
  @Override
  public void define(Context context) {
    context.addExtensions(MyDelphiRulesDefinition.class, MyDelphiFileCheckRegistrar.class);
  }
}
