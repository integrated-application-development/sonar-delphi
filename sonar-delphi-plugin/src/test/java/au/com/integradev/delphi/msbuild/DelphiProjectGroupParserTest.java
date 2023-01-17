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
package au.com.integradev.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelphiProjectGroupParserTest {
  private static final String PROJECT_GROUP =
      "/au/com/integradev/delphi/msbuild/ProjectGroup.groupproj";

  private static final String PROJECT_GROUP_WITH_INVALID_PROJECTS =
      "/au/com/integradev/delphi/msbuild/ProjectGroupWithInvalidProjects.groupproj";

  private EnvironmentVariableProvider environmentVariableProvider;
  private Path environmentProj;

  private List<DelphiProject> parse(String resource) {
    Path groupproj = DelphiUtils.getResource(resource).toPath();
    DelphiProjectGroupParser parser =
        new DelphiProjectGroupParser(groupproj, environmentVariableProvider, environmentProj);
    return parser.parse();
  }

  @BeforeEach
  void init() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    environmentProj = null;
  }

  @Test
  void testProjectGroup() {
    assertThat(parse(PROJECT_GROUP)).hasSize(3);
  }

  @Test
  void testProjectGroupWithInvalidProjects() {
    assertThat(parse(PROJECT_GROUP_WITH_INVALID_PROJECTS)).hasSize(1);
  }
}
