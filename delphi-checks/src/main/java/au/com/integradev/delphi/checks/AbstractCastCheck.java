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

import au.com.integradev.delphi.operator.BinaryOperator;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;

public abstract class AbstractCastCheck extends DelphiCheck {
  protected abstract boolean isViolation(Type originalType, Type castType);

  protected abstract String getIssueMessage();

  @Override
  public DelphiCheckContext visit(
      BinaryExpressionNode binaryExpression, DelphiCheckContext context) {
    if (binaryExpression.getOperator() == BinaryOperator.AS) {
      Type originalType = getOriginalType(binaryExpression.getLeft());
      Type castedType = getSoftCastedType(binaryExpression.getRight());

      if (isViolation(originalType, castedType)
          && !originalType.isUnknown()
          && !castedType.isUnknown()) {
        reportIssue(context, binaryExpression, getIssueMessage());
      }
    }
    return super.visit(binaryExpression, context);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    List<ExpressionNode> arguments = argumentList.getArguments();
    if (arguments.size() == 1) {
      Type originalType = getOriginalType(arguments.get(0));
      Type castedType = getHardCastedType(argumentList);
      if (castedType != null && isViolation(originalType, castedType)) {
        reportIssue(context, argumentList, getIssueMessage());
      }
    }
    return super.visit(argumentList, context);
  }

  private static Type getOriginalType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isMethod()) {
      result = ((ProceduralType) result).returnType();
    }
    return result;
  }

  private static Type getSoftCastedType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isClassReference()) {
      result = ((ClassReferenceType) result).classType();
    }
    return result;
  }

  private static Type getHardCastedType(ArgumentListNode argumentList) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = ((NameReferenceNode) previous);
      NameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration) {
        return ((TypeNameDeclaration) declaration).getType();
      }
    }

    switch (previous.getTokenType()) {
      case STRING:
        return argumentList.getTypeFactory().getIntrinsic(IntrinsicType.STRING);
      case FILE:
        return argumentList.getTypeFactory().untypedFile();
      default:
        return null;
    }
  }
}
