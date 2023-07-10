package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.check.DelphiRuleMetadataLoaderImpl;
import au.com.integradev.delphi.check.MetadataResourcePath;
import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;

public interface DelphiRuleMetadataLoader {
  void addRulesByAnnotatedClass(
      RulesDefinition.NewRepository repository, List<Class<?>> ruleClasses);

  static DelphiRuleMetadataLoader create(
      SonarRuntime sonarRuntime, MetadataResourcePath metadataResourcePath) {
    return new DelphiRuleMetadataLoaderImpl(sonarRuntime, metadataResourcePath);
  }
}
