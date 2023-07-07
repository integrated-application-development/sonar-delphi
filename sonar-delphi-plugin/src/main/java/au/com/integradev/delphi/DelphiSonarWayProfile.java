package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class DelphiSonarWayProfile implements BuiltInQualityProfilesDefinition {
  private static final Logger LOG = Loggers.get(DelphiSonarWayProfile.class);

  static final String SONAR_WAY_PATH =
      "/org/sonar/l10n/delphi/rules/community-delphi/Delphi_way_profile.json";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay =
        context.createBuiltInQualityProfile("Sonar way", Delphi.KEY);

    BuiltInQualityProfileJsonLoader.load(sonarWay, CheckList.REPOSITORY_KEY, SONAR_WAY_PATH);

    sonarWay.done();
  }
}
