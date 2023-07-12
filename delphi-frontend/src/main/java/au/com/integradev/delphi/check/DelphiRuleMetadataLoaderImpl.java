package au.com.integradev.delphi.check;

import java.util.List;
import java.util.Objects;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
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

    ruleClasses.forEach(ruleClass -> handleTemplateRule(repository, ruleClass));
  }

  private static String ruleKey(Class<?> ruleClass) {
    return AnnotationUtils.getAnnotation(ruleClass, Rule.class).key();
  }

  private static boolean isTemplateRule(Class<?> ruleClass) {
    return AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null;
  }

  private static void handleTemplateRule(NewRepository repository, Class<?> ruleClass) {
    NewRule rule = Objects.requireNonNull(repository.rule(ruleKey(ruleClass)));
    if (isTemplateRule(ruleClass)) {
      rule.setTemplate(true);
    } else {
      rule.params().removeIf(param -> param.key().equals("scope"));
    }
  }
}
