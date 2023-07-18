package au.com.integradev.delphi.check;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.EnumUtils;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class MasterCheckRegistrar {
  private final Map<String, Checks<DelphiCheck>> allChecks;
  private final SetMultimap<RuleScope, DelphiCheck> checksByScope;

  public MasterCheckRegistrar(
      CheckFactory checkFactory,
      ScopeMetadataLoader scopeMetadataLoader,
      CheckRegistrar[] checkRegistrars) {
    allChecks = new HashMap<>();
    CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
    for (CheckRegistrar checkClassesRegister : checkRegistrars) {
      checkClassesRegister.register(registrarContext);
      allChecks.put(
          registrarContext.repositoryKey(),
          checkFactory
              .<DelphiCheck>create(registrarContext.repositoryKey())
              .addAnnotatedChecks(registrarContext.getCheckClasses().toArray()));
    }

    this.checksByScope = MultimapBuilder.enumKeys(RuleScope.class).hashSetValues().build();
    for (var entry : allChecks.entrySet()) {
      String repositoryKey = entry.getKey();
      Checks<DelphiCheck> checks = entry.getValue();
      for (DelphiCheck check : checks.all()) {
        RuleKey engineKey = RuleKey.of(repositoryKey, annotatedEngineKey(check));
        RuleScope scope =
            EnumUtils.getEnum(
                RuleScope.class,
                check.customRuleScopeOverride,
                scopeMetadataLoader.getScope(engineKey));

        checksByScope.put(scope, check);
      }
    }
  }

  public Set<DelphiCheck> getChecks(RuleScope scope) {
    return checksByScope.get(scope);
  }

  public Optional<RuleKey> getRuleKey(DelphiCheck check) {
    return allChecks.values().stream()
        .map(checks -> checks.ruleKey(check))
        .filter(Objects::nonNull)
        .findFirst();
  }

  public Optional<RuleKey> getEngineKey(DelphiCheck check) {
    return allChecks.entrySet().stream()
        .filter(entry -> entry.getValue().ruleKey(check) != null)
        .map(entry -> RuleKey.of(entry.getKey(), annotatedEngineKey(check)))
        .findFirst();
  }

  private static String annotatedEngineKey(DelphiCheck check) {
    Rule ruleAnnotation = AnnotationUtils.getAnnotation(check, Rule.class);
    if (ruleAnnotation == null) {
      throw new IllegalStateException(
          "No rule annotation was found on " + check.getClass().getName());
    }
    return ruleAnnotation.key();
  }
}
