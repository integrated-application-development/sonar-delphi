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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.CastUtils;
import au.com.integradev.delphi.utils.CastUtils.DelphiCast;
import com.google.common.collect.Iterables;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "CastAndFreeRule", repositoryKey = "delph")
@Rule(key = "CastAndFree")
public class CastAndFreeCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this redundant cast.";

  @Override
  public DelphiCheckContext visit(
      PrimaryExpressionNode primaryExpression, DelphiCheckContext context) {
    Optional<DelphiCast> cast = CastUtils.readHardCast(primaryExpression, context.getTypeFactory());

    if (cast.isPresent() && isFreed(primaryExpression) && !isCastFromUntyped(cast.get())) {
      reportIssue(context, primaryExpression, MESSAGE);
    }
    return super.visit(primaryExpression, context);
  }

  @Override
  public DelphiCheckContext visit(
      BinaryExpressionNode binaryExpression, DelphiCheckContext context) {
    Optional<DelphiCast> cast = CastUtils.readSoftCast(binaryExpression);

    if (cast.isPresent() && isFreed(binaryExpression)) {
      reportIssue(context, binaryExpression, MESSAGE);
    }
    return super.visit(binaryExpression, context);
  }

  private static boolean isCastFromUntyped(DelphiCast cast) {
    Type type = cast.originalType();
    if (type.isPointer()) {
      type = ((PointerType) type).dereferencedType();
    }

    return type.isUntyped();
  }

  private static boolean isFreed(ExpressionNode expr) {
    return freeCalledOnExpression(expr) || isArgumentToFreeAndNil(expr);
  }

  private static boolean freeCalledOnExpression(ExpressionNode node) {
    if (node instanceof PrimaryExpressionNode) {
      DelphiNode lastNode = Iterables.getLast(node.getChildren());

      if (lastNode instanceof NameReferenceNode) {
        NameDeclaration lastNameDeclaration = ((NameReferenceNode) lastNode).getNameDeclaration();
        if (lastNameDeclaration instanceof RoutineNameDeclaration) {
          return ((RoutineNameDeclaration) lastNameDeclaration)
              .fullyQualifiedName()
              .equalsIgnoreCase("System.TObject.Free");
        }
      }
    }

    ExpressionNode parenthesized = node.findParentheses();
    if (node != parenthesized) {
      Node parent = parenthesized.getParent();
      return parent instanceof PrimaryExpressionNode
          && freeCalledOnExpression((PrimaryExpressionNode) parent);
    }

    return false;
  }

  private static boolean isArgumentToFreeAndNil(ExpressionNode expr) {
    DelphiNode argList = expr.findParentheses().getParent();
    if (!(argList instanceof ArgumentListNode)) {
      return false;
    }

    DelphiNode freeAndNil = argList.getParent().getChild(argList.getChildIndex() - 1);

    if (freeAndNil instanceof NameReferenceNode) {
      NameDeclaration freeAndNilDecl = ((NameReferenceNode) freeAndNil).getNameDeclaration();
      if (freeAndNilDecl instanceof RoutineNameDeclaration) {
        return ((RoutineNameDeclaration) freeAndNilDecl)
            .fullyQualifiedName()
            .equalsIgnoreCase("System.SysUtils.FreeAndNil");
      }
    }

    return false;
  }
}
