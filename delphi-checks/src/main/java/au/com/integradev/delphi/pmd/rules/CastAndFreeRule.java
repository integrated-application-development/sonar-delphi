/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import au.com.integradev.delphi.operator.BinaryOperator;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.PointerType;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.api.ast.Node;

/** Don't cast an object only to free it. */
public class CastAndFreeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ExpressionNode expr, RuleContext data) {
    if (isCastExpression(expr) && isFreed(expr) && !isAcceptableCast(expr)) {
      addViolation(data, expr);
    }
    return super.visit(expr, data);
  }

  private static boolean isCastExpression(ExpressionNode expr) {
    return isSoftCast(expr) || isHardCast(expr);
  }

  private static boolean isSoftCast(ExpressionNode expr) {
    return expr instanceof BinaryExpressionNode
        && ((BinaryExpressionNode) expr).getOperator() == BinaryOperator.AS;
  }

  private static boolean isHardCast(ExpressionNode expr) {
    return expr instanceof PrimaryExpressionNode
        && expr.jjtGetChild(0) instanceof NameReferenceNode
        && expr.jjtGetChild(1) instanceof ArgumentListNode
        && expr.jjtGetNumChildren() < 6;
  }

  private static boolean isAcceptableCast(ExpressionNode expr) {
    if (!isHardCast(expr)) {
      return false;
    }

    Type type = ((ArgumentListNode) expr.jjtGetChild(1)).getArguments().get(0).getType();
    if (type.isPointer()) {
      type = ((PointerType) type).dereferencedType();
    }

    return type.isUntyped();
  }

  private static boolean isFreed(ExpressionNode expr) {
    return isFree(expr) || isFreeAndNil(expr);
  }

  private static boolean isFree(ExpressionNode node) {
    boolean result =
        (node instanceof PrimaryExpressionNode
            && node.jjtGetChild(node.jjtGetNumChildren() - 1).getImage().equalsIgnoreCase("Free"));

    if (!result) {
      ExpressionNode parenthesized = node.findParentheses();

      if (node != parenthesized) {
        Node parent = parenthesized.jjtGetParent();
        result = parent instanceof ExpressionNode && isFree((ExpressionNode) parent);
      }
    }

    return result;
  }

  private static boolean isFreeAndNil(ExpressionNode expr) {
    DelphiNode argList = expr.findParentheses().jjtGetParent();
    DelphiNode freeAndNil = argList.jjtGetParent().jjtGetChild(argList.jjtGetChildIndex() - 1);

    return argList instanceof ArgumentListNode
        && freeAndNil instanceof NameReferenceNode
        && freeAndNil.getImage().equalsIgnoreCase("FreeAndNil");
  }
}
