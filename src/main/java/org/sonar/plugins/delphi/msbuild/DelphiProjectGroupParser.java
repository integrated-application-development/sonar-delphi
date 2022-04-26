package org.sonar.plugins.delphi.msbuild;

import java.nio.file.Path;
import java.util.List;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;

final class DelphiProjectGroupParser {
  private final Path projectGroup;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final Path environmentProj;

  DelphiProjectGroupParser(
      Path projectGroup,
      EnvironmentVariableProvider environmentVariableProvider,
      Path environmentProj) {
    this.projectGroup = projectGroup;
    this.environmentVariableProvider = environmentVariableProvider;
    this.environmentProj = environmentProj;
  }

  public List<DelphiProject> parse() {
    return new DelphiMSBuildParser(projectGroup, environmentVariableProvider, environmentProj)
        .parse()
        .getProjects();
  }
}
