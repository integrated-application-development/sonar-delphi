/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ShortIdentifiersRule", repositoryKey = "delph")
@Rule(key = "ShortIdentifier")
public class ShortIdentifierCheck extends DelphiCheck {
  private static final String MESSAGE = "Give this short identifier a more meaningful name.";
  private static final int DEFAULT_MINIMUM_LENGTH = 3;
  private static final String DEFAULT_WHITELIST = "E,I,J,K,X,Y,ID";

  @RuleProperty(
      key = "minimumLength",
      description = "Minimum length of an identifier.",
      defaultValue = DEFAULT_MINIMUM_LENGTH + "")
  public int minimumLength = DEFAULT_MINIMUM_LENGTH;

  @RuleProperty(
      key = "whitelist",
      description =
          "Comma-delimited list of short identifiers that are allowed. (case-insensitive)",
      defaultValue = DEFAULT_WHITELIST)
  public String whitelist = DEFAULT_WHITELIST;

  private Set<String> whitelistSet;

  @Override
  public void start(DelphiCheckContext context) {
    whitelistSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(whitelist));
  }

  @Override
  public DelphiCheckContext visit(UnitImportNode node, DelphiCheckContext context) {
    // If a unit name is too short, we want to flag it in that file instead.
    return context;
  }

  @Override
  public DelphiCheckContext visit(GenericDefinitionNode node, DelphiCheckContext context) {
    // We never want to check type parameters.
    return context;
  }

  @Override
  public DelphiCheckContext visit(NameDeclarationNode node, DelphiCheckContext context) {
    String image = node.getImage();
    if (image.length() < minimumLength && !whitelistSet.contains(image)) {
      reportIssue(context, node, MESSAGE);
    }
    return super.visit(node, context);
  }
}
