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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedPropertiesRule", repositoryKey = "delph")
@Rule(key = "UnusedProperty")
public class UnusedPropertyCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused property.";

  @RuleProperty(
      key = "excludeApi",
      description = "Exclude properties declared in the interface section with public visibility.")
  public boolean excludeApi = false;

  @Override
  public DelphiCheckContext visit(PropertyNode property, DelphiCheckContext context) {
    NameDeclarationNode name = property.getPropertyName();
    PropertyNameDeclaration declaration = (PropertyNameDeclaration) name.getNameDeclaration();
    if (isViolation(declaration)) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }

  private boolean isViolation(PropertyNameDeclaration declaration) {
    if (declaration.isPublished()) {
      return false;
    }

    if (excludeApi && declaration.isPublic() && !declaration.isImplementationDeclaration()) {
      return false;
    }

    if (!declaration.getAttributeTypes().isEmpty()) {
      return false;
    }

    return declaration.getScope().getOccurrencesFor(declaration).isEmpty()
        && declaration.getRedeclarations().stream().allMatch(this::isViolation);
  }
}
