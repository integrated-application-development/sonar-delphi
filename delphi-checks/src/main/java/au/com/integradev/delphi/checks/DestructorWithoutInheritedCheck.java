/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "DestructorWithoutInheritedStatementRule", repositoryKey = "delph")
@Rule(key = "DestructorWithoutInherited")
public class DestructorWithoutInheritedCheck extends AbstractWithoutInheritedCheck {
  @RuleProperty(
      key = "destructorLikes",
      description =
          "Comma-delimited list of unqualified method names to treat like destructors for this"
              + " rule.")
  public String destructorLikes = "";

  private Set<String> destructorLikesSet;

  @Override
  public void start(DelphiCheckContext context) {
    destructorLikesSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(destructorLikes));
  }

  @Override
  protected String getIssueMessage() {
    return "Add an 'inherited' statement to this destructor.";
  }

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    if (isDestructorLike(method)) {
      checkViolation(method, context);
    }
    return super.visit(method, context);
  }

  private boolean isDestructorLike(MethodImplementationNode method) {
    if (method.isDestructor()) {
      return true;
    }

    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration != null) {
      String name = declaration.getName();

      return declaration.hasDirective(MethodDirective.OVERRIDE)
          && destructorLikesSet.contains(name);
    }

    return false;
  }
}
