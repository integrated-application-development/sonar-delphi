package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class DelphiSonarWayProfile implements BuiltInQualityProfilesDefinition {
  private final DelphiSonarWayResourcePath resourcePath;

  public DelphiSonarWayProfile(DelphiSonarWayResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay =
        context.createBuiltInQualityProfile("Sonar way", Delphi.KEY);

    BuiltInQualityProfileJsonLoader.load(sonarWay, CheckList.REPOSITORY_KEY, resourcePath.get());

    sonarWay.done();
  }
}
