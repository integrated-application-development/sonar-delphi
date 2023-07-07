package au.com.integradev.delphi.check;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class MasterCheckRegistrar {
  private final List<Checks<DelphiCheck>> allChecks;
  private final SetMultimap<RuleScope, DelphiCheck> checksByScope;

  public MasterCheckRegistrar(
      CheckFactory checkFactory,
      ScopeMetadataLoader scopeMetadataLoader,
      CheckRegistrar[] checkRegistrars) {
    this.allChecks = new ArrayList<>();
    CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
    for (CheckRegistrar checkClassesRegister : checkRegistrars) {
      checkClassesRegister.register(registrarContext);
      allChecks.add(
          checkFactory
              .<DelphiCheck>create(registrarContext.repositoryKey())
              .addAnnotatedChecks(registrarContext.getCheckClasses().toArray()));
    }

    this.checksByScope = MultimapBuilder.enumKeys(RuleScope.class).hashSetValues().build();
    for (Checks<DelphiCheck> checks : allChecks) {
      for (DelphiCheck check : checks.all()) {
        RuleScope scope = scopeMetadataLoader.getScope(checks.ruleKey(check));
        checksByScope.put(scope, check);
      }
    }
  }

  public Set<DelphiCheck> getChecks(RuleScope scope) {
    return checksByScope.get(scope);
  }

  public Optional<RuleKey> getRuleKey(DelphiCheck check) {
    return allChecks.stream()
        .map(sonarChecks -> sonarChecks.ruleKey(check))
        .filter(Objects::nonNull)
        .findFirst();
  }
}
