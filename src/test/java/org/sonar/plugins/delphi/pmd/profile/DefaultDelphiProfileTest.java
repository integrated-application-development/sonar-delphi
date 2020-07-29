package org.sonar.plugins.delphi.pmd.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.utils.PmdLevelUtils.fromLevel;

import java.util.List;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInActiveRule;
import org.sonar.plugins.delphi.core.DelphiLanguage;

public class DefaultDelphiProfileTest {

  private static RuleFinder createRuleFinder() {
    RuleFinder ruleFinder = mock(RuleFinder.class);
    when(ruleFinder.find(any(RuleQuery.class)))
        .then(
            (Answer<Rule>)
                invocation -> {
                  RuleQuery query = (RuleQuery) invocation.getArguments()[0];
                  return Rule.create(query.getRepositoryKey(), query.getKey(), "")
                      .setConfigKey("ConfigKey:" + query.getKey())
                      .setSeverity(fromLevel(1));
                });
    return ruleFinder;
  }

  @Test
  public void testShouldCreateDefaultDelphiProfile() {
    DelphiPmdProfileImporter importer = new DelphiPmdProfileImporter(createRuleFinder());
    DefaultDelphiProfile profileDef = new DefaultDelphiProfile(importer);

    BuiltInQualityProfilesDefinition.Context context =
        new BuiltInQualityProfilesDefinition.Context();
    profileDef.define(context);

    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile =
        context.profile(DelphiLanguage.KEY, DefaultDelphiProfile.DEFAULT_PROFILE_NAME);

    List<BuiltInActiveRule> activeRules = profile.rules();
    assertThat(activeRules).hasSize(51);
    assertThat(profile.name()).isEqualTo(DefaultDelphiProfile.DEFAULT_PROFILE_NAME);

    // Check that we use severity from the read rule and not default one.
    for (BuiltInActiveRule rule : activeRules) {
      assertThat(rule.overriddenSeverity()).isNotNull();
    }
  }
}
