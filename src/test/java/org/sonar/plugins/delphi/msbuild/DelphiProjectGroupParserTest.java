package org.sonar.plugins.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiProjectGroupParserTest {
  private static final String PROJECT_GROUP =
      "/org/sonar/plugins/delphi/msbuild/ProjectGroup.groupproj";

  private static final String PROJECT_GROUP_WITH_INVALID_PROJECTS =
      "/org/sonar/plugins/delphi/msbuild/ProjectGroupWithInvalidProjects.groupproj";

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
