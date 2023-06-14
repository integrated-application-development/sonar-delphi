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
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ForbiddenConstantRule", repositoryKey = "delph")
@Rule(key = "ForbiddenConstant")
public class ForbiddenConstantCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden constant.";

  @RuleProperty(key = "unitName", description = "Name of the unit whose constants are forbidden")
  public String unitName = "";

  @RuleProperty(key = "constants", description = "Comma-delimited list of forbidden constants")
  public String constants = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Set<String> constantsSet;

  @Override
  public void start(DelphiCheckContext context) {
    constantsSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().splitToList(constants));
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      DelphiScope scope = declaration.getScope();
      if (scope instanceof FileScope) {
        String constantUnitName = ((FileScope) scope).getUnitDeclaration().fullyQualifiedName();
        String constantName = declaration.getName();
        if (unitName.equalsIgnoreCase(constantUnitName) && constantsSet.contains(constantName)) {
          reportIssue(context, reference.getIdentifier(), message);
        }
      }
    }
    return super.visit(reference, context);
  }
}
