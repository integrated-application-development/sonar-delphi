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
package au.com.integradev.delphi.checks;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@RuleTemplate
@DeprecatedRuleKey(ruleKey = "ForbiddenIdentifierRule", repositoryKey = "delph")
@Rule(key = "ForbiddenIdentifier")
public class ForbiddenIdentifierCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden identifier.";

  @RuleProperty(
      key = "blacklist",
      description = "Comma-delimited list of forbidden identifiers (case-insensitive)")
  public String blacklist = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Set<String> identifiersSet;

  @Override
  public void start(DelphiCheckContext context) {
    identifiersSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(blacklist));
  }

  @Override
  public DelphiCheckContext visit(NameDeclarationNode node, DelphiCheckContext context) {
    if (identifiersSet.contains(node.getImage())) {
      reportIssue(context, node, message);
    }
    return super.visit(node, context);
  }
}
