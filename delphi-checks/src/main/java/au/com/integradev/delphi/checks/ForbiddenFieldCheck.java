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
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ForbiddenFieldRule", repositoryKey = "delph")
@Rule(key = "ForbiddenField")
public class ForbiddenFieldCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Remove usage of this forbidden field.";

  @RuleProperty(
      key = "typeName",
      description = "Fully qualified name of the type whose values are forbidden")
  private String typeName = "";

  @RuleProperty(key = "fields", description = "Comma-delimited list of forbidden fields")
  private String fields = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Set<String> fieldsSet;

  @Override
  public void start(DelphiCheckContext context) {
    fieldsSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(fields));
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      TypeScope scope = declaration.getScope().getEnclosingScope(TypeScope.class);
      if (scope != null) {
        Type type = scope.getType();
        String fieldName = declaration.getName();
        if (type.is(typeName) && fieldsSet.contains(fieldName)) {
          reportIssue(context, reference.getIdentifier(), message);
        }
      }
    }
    return super.visit(reference, context);
  }
}
