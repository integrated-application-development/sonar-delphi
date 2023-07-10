package au.com.integradev.delphi;

import au.com.integradev.delphi.check.DelphiRuleMetadataLoaderImpl;
import au.com.integradev.delphi.check.MetadataResourcePath;
import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.communitydelphi.api.check.DelphiRuleMetadataLoader;

public class DelphiRulesDefinition implements RulesDefinition {
  private final DelphiRuleMetadataLoader ruleMetadataLoader;
  private final DelphiSonarWayResourcePath sonarWayResourcePath;

  public DelphiRulesDefinition(
      SonarRuntime sonarRuntime,
      MetadataResourcePath metadataResourcePath,
      DelphiSonarWayResourcePath sonarWayResourcePath) {
    this.ruleMetadataLoader = DelphiRuleMetadataLoader.create(sonarRuntime, metadataResourcePath);
    this.sonarWayResourcePath = sonarWayResourcePath;
  }

  @Override
  public void define(Context context) {
    NewRepository repository =
        context.createRepository(CheckList.REPOSITORY_KEY, Delphi.KEY).setName("Community Delphi");

    ((DelphiRuleMetadataLoaderImpl) ruleMetadataLoader)
        .setDefaultProfilePath(sonarWayResourcePath.get());

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getChecks());

    repository.done();
  }
}
