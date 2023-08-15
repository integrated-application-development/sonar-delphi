/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplateAnnotationReader;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

/**
 * Declare rule metadata in server repository of rules. That allows SonarQube to list the rules in
 * the "Rules" page.
 */
public class MyDelphiRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_KEY = "mycompany-delphi";
  public static final String REPOSITORY_NAME = "MyCompany Custom Repository";

  private final SonarRuntime runtime;
  private final MetadataResourcePath metadataResourcePath;

  public MyDelphiRulesDefinition(SonarRuntime runtime, MetadataResourcePath metadataResourcePath) {
    this.runtime = runtime;
    this.metadataResourcePath = metadataResourcePath;
  }

  @Override
  public void define(Context context) {
    NewRepository repository =
        context.createRepository(REPOSITORY_KEY, "delphi").setName(REPOSITORY_NAME);

    String resourceFolder = metadataResourcePath.forRepository(repository.key());

    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(resourceFolder, runtime);
    RuleTemplateAnnotationReader ruleTemplateAnnotationReader = new RuleTemplateAnnotationReader();

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, RulesList.getChecks());
    ruleTemplateAnnotationReader.updateRulesByAnnotatedClass(repository, RulesList.getChecks());

    repository.done();
  }
}
