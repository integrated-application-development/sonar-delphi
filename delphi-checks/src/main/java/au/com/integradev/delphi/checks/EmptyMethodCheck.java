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

import au.com.integradev.delphi.utils.InterfaceUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyMethodRule", repositoryKey = "delph")
@Rule(key = "EmptyMethod")
public class EmptyMethodCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty method.";

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    if (method.isEmptyMethod() && shouldAddViolation(method)) {
      reportIssue(context, method.getMethodNameNode(), MESSAGE);
    }
    return super.visit(method, context);
  }

  private static boolean shouldAddViolation(MethodImplementationNode method) {
    DelphiNode block = method.getBlock();

    if (block != null && block.getComments().isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration == null) {
      return true;
    }

    return !declaration.hasDirective(MethodDirective.OVERRIDE)
        && !declaration.hasDirective(MethodDirective.VIRTUAL)
        && !InterfaceUtils.implementsMethodOnInterface(declaration);
  }
}
