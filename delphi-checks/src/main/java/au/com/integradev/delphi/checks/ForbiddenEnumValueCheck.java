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
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.EnumElementNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@RuleTemplate
@DeprecatedRuleKey(ruleKey = "ForbiddenEnumValueRule", repositoryKey = "delph")
@Rule(key = "ForbiddenEnumValue")
public class ForbiddenEnumValueCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden enum value.";

  @RuleProperty(
      key = "enumName",
      description = "Fully qualified name of the enum whose values are forbidden")
  public String enumName = "";

  @RuleProperty(key = "blacklist", description = "Comma-delimited list of forbidden enum values")
  public String blacklist = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Set<String> valuesSet;

  @Override
  public void start(DelphiCheckContext context) {
    valuesSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(blacklist));
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof EnumElementNameDeclaration) {
      var element = (EnumElementNameDeclaration) declaration;
      if (element.getType().is(enumName) && valuesSet.contains(element.getName())) {
        reportIssue(context, reference, message);
      }
    }
    return super.visit(reference, context);
  }
}
