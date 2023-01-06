/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.plugins.delphi.pmd.xml;

import java.io.IOException;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.pmd.xml.factory.ActiveRulesRuleSetFactory;
import org.sonar.plugins.delphi.pmd.xml.factory.RuleSetFactory;
import org.sonar.plugins.delphi.pmd.xml.factory.RulesProfileRuleSetFactory;
import org.sonar.plugins.delphi.pmd.xml.factory.XmlRuleSetFactory;

/** Utility class for creating {@link DelphiRuleSet} instances from different inputs. */
public class DelphiRuleSetHelper {
  private static final Logger LOG = Loggers.get(DelphiRuleSetHelper.class);

  private DelphiRuleSetHelper() {
    // utility class
  }

  /**
   * Create a DelphiRuleSet from a PMD XML configuration file, reporting errors via Sonar
   * ValidationMessages
   *
   * @param configReader A character stream containing the data of the {@link DelphiRuleSet}.
   * @param messages Sonar validation messages - used to report processing problems.
   * @return instance of DelphiRuleSet. Empty if an error occurs.
   */
  @NotNull
  public static DelphiRuleSet createFrom(Reader configReader, ValidationMessages messages) {
    return createQuietly(new XmlRuleSetFactory(configReader, messages));
  }

  /**
   * Create a DelphiRuleSet from a PMD XML configuration file
   *
   * @param configReader A character stream containing the data of the {@link DelphiRuleSet}.
   * @return An instance of DelphiRuleSet. Empty if an error occurs.
   */
  @NotNull
  public static DelphiRuleSet createFrom(Reader configReader) {
    return createQuietly(new XmlRuleSetFactory(configReader, null));
  }

  /**
   * Create a DelphiRuleSet from a Sonar ActiveRules object
   *
   * @param activeRules The currently active rules.
   * @param repositoryKey The key identifier of the rule repository.
   * @return instance of DelphiRuleSet. Empty if an error occurs.
   */
  @NotNull
  public static DelphiRuleSet createFrom(ActiveRules activeRules, String repositoryKey) {
    return create(new ActiveRulesRuleSetFactory(activeRules, repositoryKey));
  }

  /**
   * Create a DelphiRuleSet from a Sonar RulesProfile object
   *
   * @param rulesProfile The current rules profile.
   * @param repositoryKey The key identifier of the rule repository.
   * @return instance of DelphiRuleSet. Empty if an error occurs.
   */
  @NotNull
  public static DelphiRuleSet createFrom(RulesProfile rulesProfile, String repositoryKey) {
    return create(new RulesProfileRuleSetFactory(rulesProfile, repositoryKey));
  }

  private static DelphiRuleSet create(RuleSetFactory factory) {
    return factory.create();
  }

  private static DelphiRuleSet createQuietly(XmlRuleSetFactory factory) {

    final DelphiRuleSet result = create(factory);

    try {
      factory.close();
    } catch (IOException e) {
      LOG.warn("Failed to close the given resource.", e);
    }

    return result;
  }
}
