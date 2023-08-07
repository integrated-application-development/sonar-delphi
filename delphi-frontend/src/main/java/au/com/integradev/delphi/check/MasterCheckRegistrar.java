package au.com.integradev.delphi.check;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
  private final IdentityHashMap<DelphiCheck, RuleScope> scopesByCheck;

  public MasterCheckRegistrar(CheckFactory checkFactory, CheckRegistrar[] checkRegistrars) {
    allChecks = new HashMap<>();
    checksByScope = MultimapBuilder.enumKeys(RuleScope.class).hashSetValues().build();
    scopesByCheck = new IdentityHashMap<>();

    CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
    for (CheckRegistrar checkClassesRegister : checkRegistrars) {
      checkClassesRegister.register(registrarContext);

      Checks<DelphiCheck> checks =
          checkFactory
              .<DelphiCheck>create(registrarContext.repositoryKey())
              .addAnnotatedChecks(registrarContext.checkClasses().toArray());

      allChecks.put(registrarContext.repositoryKey(), checks);

      for (DelphiCheck check : checks.all()) {
        RuleKey engineKey = RuleKey.of(registrarContext.repositoryKey(), annotatedEngineKey(check));
        RuleScope scope =
            EnumUtils.getEnum(
                RuleScope.class,
                check.customRuleScopeOverride,
                registrarContext.getScopeFunction().apply(engineKey));

        checksByScope.put(scope, check);
        scopesByCheck.put(check, scope);
      }
    }
  }

  public Set<DelphiCheck> getChecks(RuleScope scope) {
    return checksByScope.get(scope);
  }

  public RuleScope getScope(DelphiCheck check) {
    return scopesByCheck.get(check);
  }

  public Optional<RuleKey> getRuleKey(DelphiCheck check) {
    return allChecks.values().stream()
        .map(checks -> checks.ruleKey(check))
        .filter(Objects::nonNull)
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
