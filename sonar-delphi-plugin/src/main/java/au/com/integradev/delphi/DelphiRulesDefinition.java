package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplateAnnotationReader;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class DelphiRulesDefinition implements RulesDefinition {
  private final SonarRuntime runtime;
  private final MetadataResourcePath metadataResourcePath;
  private final DelphiSonarWayResourcePath sonarWayResourcePath;

  public DelphiRulesDefinition(
      SonarRuntime runtime,
      MetadataResourcePath metadataResourcePath,
      DelphiSonarWayResourcePath sonarWayResourcePath) {
    this.runtime = runtime;
    this.metadataResourcePath = metadataResourcePath;
    this.sonarWayResourcePath = sonarWayResourcePath;
  }

  @Override
  public void define(Context context) {
    NewRepository repository =
        context.createRepository(CheckList.REPOSITORY_KEY, Delphi.KEY).setName("Community Delphi");

    RuleMetadataLoader ruleMetadataLoader =
        new RuleMetadataLoader(
            metadataResourcePath.forRepository(repository.key()),
            sonarWayResourcePath.get(),
            runtime);

    RuleTemplateAnnotationReader ruleTemplateAnnotationReader = new RuleTemplateAnnotationReader();

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getChecks());
    ruleTemplateAnnotationReader.updateRulesByAnnotatedClass(repository, CheckList.getChecks());

    repository.done();
  }
}
