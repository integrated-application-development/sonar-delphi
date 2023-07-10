package au.com.integradev.delphi.check;

import java.util.List;
import java.util.Objects;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiRuleMetadataLoader;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class DelphiRuleMetadataLoaderImpl implements DelphiRuleMetadataLoader {
  private final SonarRuntime sonarRuntime;
  private final MetadataResourcePath metadataResourcePath;
  private String defaultProfilePath;

  public DelphiRuleMetadataLoaderImpl(
      SonarRuntime sonarRuntime, MetadataResourcePath metadataResourcePath) {
    this.sonarRuntime = sonarRuntime;
    this.metadataResourcePath = metadataResourcePath;
  }

  public void setDefaultProfilePath(String defaultProfilePath) {
    this.defaultProfilePath = defaultProfilePath;
  }

  @Override
  public void addRulesByAnnotatedClass(
      RulesDefinition.NewRepository repository, List<Class<?>> ruleClasses) {
    RuleMetadataLoader ruleMetadataLoader =
        new RuleMetadataLoader(
            metadataResourcePath.forRepository(repository.key()), defaultProfilePath, sonarRuntime);
    ruleMetadataLoader.addRulesByAnnotatedClass(repository, ruleClasses);

    ruleClasses.stream()
        .filter(DelphiRuleMetadataLoaderImpl::isTemplateRule)
        .map(DelphiRuleMetadataLoaderImpl::ruleKey)
        .map(repository::rule)
        .map(Objects::requireNonNull)
        .forEach(rule -> rule.setTemplate(true));
  }

  private static String ruleKey(Class<?> rule) {
    return AnnotationUtils.getAnnotation(rule, Rule.class).key();
  }

  private static boolean isTemplateRule(Class<?> rule) {
    return AnnotationUtils.getAnnotation(rule, RuleTemplate.class) != null;
  }
}
