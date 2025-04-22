/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MSBuildStateTest {
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
    var state = new MSBuildState(tempDir, tempDir, environmentVariableProvider);

    assertThat(state.getProperty("FOO")).isEmpty();
    assertThat(state.getProperty("BAR")).isEmpty();
    assertThat(state.getProperty("BAZ")).isEqualTo("flarp");
  }

  @Test
  void testWellKnownProperties() {
    var state = new MSBuildState(tempDir, tempDir, environmentVariableProvider);
    assertThat(state.getProperty("MSBuildThisFileFullPath")).isEqualTo(tempDir.toString());
  }

  @Test
  void testOverrideWellKnownProperties() {
    var state = new MSBuildState(tempDir, tempDir, environmentVariableProvider);
    state.setProperty("MSBuildThisFileFullPath", "bonk");
    assertThat(state.getProperty("MSBuildThisFileFullPath")).isEqualTo("bonk");
  }
}
