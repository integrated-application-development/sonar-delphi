/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.communitydelphi.pmd.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.utils.PmdLevelUtils.fromLevel;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInActiveRule;
import org.sonar.plugins.communitydelphi.core.DelphiLanguage;

class DefaultDelphiProfileTest {

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
  void testShouldCreateDefaultDelphiProfile() {
    DelphiPmdProfileImporter importer = new DelphiPmdProfileImporter(createRuleFinder());
    DefaultDelphiProfile profileDef = new DefaultDelphiProfile(importer);

    BuiltInQualityProfilesDefinition.Context context =
        new BuiltInQualityProfilesDefinition.Context();
    profileDef.define(context);

    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile =
        context.profile(DelphiLanguage.KEY, DefaultDelphiProfile.DEFAULT_PROFILE_NAME);

    List<BuiltInActiveRule> activeRules = profile.rules();
    assertThat(activeRules).hasSize(70);
    assertThat(profile.name()).isEqualTo(DefaultDelphiProfile.DEFAULT_PROFILE_NAME);

    // Check that we use severity from the read rule and not default one.
    for (BuiltInActiveRule rule : activeRules) {
      assertThat(rule.overriddenSeverity()).isNotNull();
    }
  }
}
