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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.NameReferenceNode;
import au.com.integradev.delphi.antlr.ast.node.PrimaryExpressionNode;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclaration;
import au.com.integradev.delphi.type.Typed;
import net.sourceforge.pmd.RuleContext;

public class ObjectInvokedConstructorRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    if (isConstructor(reference) && isInvokedOnObject(reference)) {
      addViolation(data, reference.getIdentifier());
    }
    return super.visit(reference, data);
  }

  private static boolean isConstructor(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  private static boolean isInvokedOnObject(NameReferenceNode reference) {
    if (reference.getFirstParentOfType(PrimaryExpressionNode.class) == null) {
      return false;
    }

    NameReferenceNode previous = reference.prevName();
    if (previous == null) {
      return false;
    }

    NameDeclaration declaration = previous.getNameDeclaration();
    return declaration instanceof VariableNameDeclaration
        && !((Typed) declaration).getType().isClassReference()
        && !previous.getNameOccurrence().isSelf();
  }
}
