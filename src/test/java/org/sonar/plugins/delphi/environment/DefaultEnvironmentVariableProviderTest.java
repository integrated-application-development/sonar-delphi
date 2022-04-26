package org.sonar.plugins.delphi.environment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.enviroment.DefaultEnvironmentVariableProvider;

class DefaultEnvironmentVariableProviderTest {
  @Test
  void testGetAllEnvironmentVariables() {
    var provider = new DefaultEnvironmentVariableProvider();
    assertThat(System.getenv()).containsExactlyEntriesOf(provider.getenv());
  }

  @Test
  void testGetSingleEnvironmentVariable() {
    var provider = new DefaultEnvironmentVariableProvider();
    for (var entry : System.getenv().entrySet()) {
      assertThat(provider.getenv(entry.getKey())).isEqualTo(entry.getValue());
    }
  }
}
