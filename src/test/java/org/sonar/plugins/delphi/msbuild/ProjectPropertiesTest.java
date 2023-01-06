/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
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
