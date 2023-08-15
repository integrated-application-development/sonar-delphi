/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;

class MyDelphiRulesPluginTest {
  @Test
  void testDefine() {
    MyDelphiRulesPlugin plugin = new MyDelphiRulesPlugin();

    Plugin.Context context = new Plugin.Context(mock());
    plugin.define(context);

    assertThat((List<?>) context.getExtensions()).hasSize(2);
  }
}
