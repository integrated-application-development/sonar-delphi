/*
 * Sonar Delphi Plugin
 * Copyright (C) 2021 Integrated Application Development
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
package au.com.integradev.delphi.pmd.rules;

import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import net.sourceforge.pmd.RuleContext;

public class FreeAndNilTObjectRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(PrimaryExpressionNode expression, RuleContext data) {
    if (isViolation(expression)) {
      addViolation(data, expression);
    }
    return super.visit(expression, data);
  }

  private static boolean isViolation(PrimaryExpressionNode expression) {
    if (!(expression.jjtGetChild(0) instanceof NameReferenceNode)) {
      return false;
    }

    if (!(expression.jjtGetChild(1) instanceof ArgumentListNode)) {
      return false;
    }

    NameReferenceNode reference = (NameReferenceNode) expression.jjtGetChild(0);
    if (reference.nextName() != null) {
      return false;
    }

    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration method = (MethodNameDeclaration) declaration;
      if (method.fullyQualifiedName().equals("System.SysUtils.FreeAndNil")) {
        ArgumentListNode argumentList = (ArgumentListNode) expression.jjtGetChild(1);
        ExpressionNode argument = argumentList.getArguments().get(0);
        return !argument.getType().isUnresolved()
            && !argument.getType().isUnknown()
            && !argument.getType().isClass();
      }
    }

    return false;
  }
}
