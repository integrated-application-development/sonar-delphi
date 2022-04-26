package org.sonar.plugins.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class ProjectPropertiesTest {
  private static final String ENVIRONMENT_PROJ =
      "/org/sonar/plugins/delphi/msbuild/environment.proj";

  private EnvironmentVariableProvider environmentVariableProvider;

  @TempDir private Path tempDir;

  @BeforeEach
  void init() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Map.of("BAZ", "flarp"));
    when(environmentVariableProvider.getenv("BAZ")).thenReturn("flarp");
  }

  @Test
  void testProjectPropertiesConstructedFromEnvironmentVariables() {
    var properties = ProjectProperties.create(environmentVariableProvider, null);

    assertThat(properties.get("FOO")).isNull();
    assertThat(properties.get("BAR")).isNull();
    assertThat(properties.get("BAZ")).isEqualTo("flarp");
  }

  @Test
  void testProjectPropertiesConstructedFromEnvironmentVariablesAndEnvironmentProj() {
    Path environmentProj = DelphiUtils.getResource(ENVIRONMENT_PROJ).toPath();
    var properties = ProjectProperties.create(environmentVariableProvider, environmentProj);

    assertThat(properties.get("FOO")).isEqualTo("foo");
    assertThat(properties.get("BAR")).isEqualTo("bar");
    assertThat(properties.get("BAZ")).isEqualTo("flarp");
  }

  @Test
  void testProjectPropertiesConstructedFromEnvironmentVariablesAndInvalidEnvironmentProj() {
    Path environmentProj = tempDir.resolve("does_not_exist.proj");
    var properties = ProjectProperties.create(environmentVariableProvider, environmentProj);

    assertThat(properties.get("FOO")).isNull();
    assertThat(properties.get("BAR")).isNull();
    assertThat(properties.get("BAZ")).isEqualTo("flarp");
  }
}
