/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.utils;

import java.util.List;
import java.util.Optional;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class CastUtils {
  private CastUtils() {
    // Utility class
  }

  public static Optional<DelphiCast> readSoftCast(BinaryExpressionNode binaryExpression) {
    if (binaryExpression.getOperator() != BinaryOperator.AS) {
      return Optional.empty();
    }

    Type originalType = getOriginalType(binaryExpression.getLeft());
    Type castedType = getSoftCastedType(binaryExpression.getRight());
    return Optional.of(new DelphiCast(originalType, castedType));
  }

  public static Optional<DelphiCast> readHardCast(
      PrimaryExpressionNode primaryExpression, TypeFactory typeFactory) {
    List<DelphiNode> children = primaryExpression.getChildren();
    if (children.size() < 2
        || !(children.get(1) instanceof ArgumentListNode)
        || !isValidHardCastHeader(children.get(0))) {
      return Optional.empty();
    }

    ArgumentListNode argumentList = (ArgumentListNode) primaryExpression.getChild(1);
    List<ExpressionNode> arguments = argumentList.getArguments();
    if (arguments.size() != 1) {
      return Optional.empty();
    }
    Type originalType = getOriginalType(arguments.get(0));

    DelphiNode nameReference = primaryExpression.getChild(0);
    Type castedType = getHardCastedType(nameReference, typeFactory);

    return castedType == null
        ? Optional.empty()
        : Optional.of(new DelphiCast(originalType, castedType));
  }

  private static boolean isValidHardCastHeader(DelphiNode node) {
    return node instanceof NameReferenceNode
        || node.getTokenType() == DelphiTokenType.STRING
        || node.getTokenType() == DelphiTokenType.FILE;
  }

  private static Type getOriginalType(ExpressionNode expression) {
    Type result = expression.getType();
    if (result.isRoutine()) {
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

  private static Type getHardCastedType(DelphiNode node, TypeFactory typeFactory) {
    if (node instanceof NameReferenceNode) {
      NameDeclaration declaration = ((NameReferenceNode) node).getLastName().getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration) {
        return ((TypeNameDeclaration) declaration).getType();
      }
    }

    switch (node.getTokenType()) {
      case STRING:
        return typeFactory.getIntrinsic(IntrinsicType.STRING);
      case FILE:
        return typeFactory.untypedFile();
      default:
        return null;
    }
  }

  public static class DelphiCast {
    private final Type originalType;
    private final Type castedType;

    private DelphiCast(Type originalType, Type castedType) {
      this.originalType = originalType;
      this.castedType = castedType;
    }

    public Type originalType() {
      return originalType;
    }

    public Type castedType() {
      return castedType;
    }
  }
}
