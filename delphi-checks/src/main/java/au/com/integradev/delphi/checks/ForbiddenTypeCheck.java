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
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@RuleTemplate
@DeprecatedRuleKey(ruleKey = "ForbiddenTypeRule", repositoryKey = "delph")
@Rule(key = "ForbiddenType")
public class ForbiddenTypeCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden type.";

  @RuleProperty(
      key = "blacklist",
      description =
          "Comma-delimited list of forbidden (fully qualified) type names (case-insensitive)")
  public String blacklist = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Set<String> typesSet;

  @Override
  public void start(DelphiCheckContext context) {
    typesSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(blacklist));
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof TypeNameDeclaration) {
      String fullyQualifiedMethodName = ((TypeNameDeclaration) declaration).fullyQualifiedName();
      if (typesSet.contains(fullyQualifiedMethodName)) {
        reportIssue(context, reference.getIdentifier(), message);
      }
    }
    return super.visit(reference, context);
  }

  @Override
  public DelphiCheckContext visit(MethodNameNode methodName, DelphiCheckContext context) {
    // It would be rude to flag a type's method implementations just for existing.
    return context;
  }
}
