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
package org.sonar.plugins.communitydelphi.api.ast.utils;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.DecimalLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public final class ExpressionNodeUtils {
  private ExpressionNodeUtils() {
    // Utility class
  }

  public static boolean isInherited(ExpressionNode node) {
    node = node.skipParentheses();
    return node instanceof PrimaryExpressionNode
        && node.getChild(0).getTokenType() == DelphiTokenType.INHERITED;
  }

  public static boolean isBareInherited(ExpressionNode node) {
    return node instanceof PrimaryExpressionNode
        && node.getChild(0).getTokenType() == DelphiTokenType.INHERITED
        && (node.getChildren().size() == 1 || !(node.getChild(1) instanceof NameReferenceNode));
  }

  @Nullable
  public static IntegerLiteralNode unwrapInteger(ExpressionNode node) {
    return unwrap(node, IntegerLiteralNode.class);
  }

  @Nullable
  public static DecimalLiteralNode unwrapDecimal(ExpressionNode node) {
    return unwrap(node, DecimalLiteralNode.class);
  }

  @Nullable
  public static TextLiteralNode unwrapText(ExpressionNode node) {
    return unwrap(node, TextLiteralNode.class);
  }

  @Nullable
  public static NilLiteralNode unwrapNil(ExpressionNode node) {
    return unwrap(node, NilLiteralNode.class);
  }

  public static boolean isIntegerLiteral(ExpressionNode node) {
    IntegerLiteralNode literal = unwrapInteger(node);
    return literal != null;
  }

  public static boolean isDecimalLiteral(ExpressionNode node) {
    IntegerLiteralNode literal = unwrapInteger(node);
    return literal != null && literal.getRadix() == 10;
  }

  public static boolean isHexadecimalLiteral(ExpressionNode node) {
    IntegerLiteralNode literal = unwrapInteger(node);
    return literal != null && literal.getRadix() == 16;
  }

  public static boolean isBinaryLiteral(ExpressionNode node) {
    IntegerLiteralNode literal = unwrapInteger(node);
    return literal != null && literal.getRadix() == 2;
  }

  public static boolean isRealLiteral(ExpressionNode node) {
    return unwrapDecimal(node) != null;
  }

  public static boolean isTextLiteral(ExpressionNode node) {
    return unwrapText(node) != null;
  }

  public static boolean isNilLiteral(ExpressionNode node) {
    return unwrapNil(node) != null;
  }

  public static boolean isBooleanLiteral(ExpressionNode node) {
    return isTrue(node) || isFalse(node);
  }

  public static boolean isTrue(ExpressionNode node) {
    return isReferenceToSystemConstant(node, "True");
  }

  public static boolean isFalse(ExpressionNode node) {
    return isReferenceToSystemConstant(node, "False");
  }

  public static boolean isResult(ExpressionNode node) {
    NameReferenceNode reference = unwrap(node, NameReferenceNode.class);
    if (reference == null) {
      return false;
    }

    NameDeclaration declaration = reference.getLastName().getNameDeclaration();
    return declaration instanceof VariableNameDeclaration
        && ((VariableNameDeclaration) declaration).isResult();
  }

  private static boolean isReferenceToSystemConstant(ExpressionNode node, String name) {
    NameReferenceNode reference = unwrap(node, NameReferenceNode.class);
    if (reference == null) {
      return false;
    }

    NameDeclaration declaration = reference.getLastName().getNameDeclaration();
    if (!(declaration instanceof VariableNameDeclaration)) {
      return false;
    }

    VariableNameDeclaration variableDeclaration = (VariableNameDeclaration) declaration;
    if (!variableDeclaration.isConst()) {
      return false;
    }

    if (!(variableDeclaration.getScope() instanceof SystemScope)) {
      return false;
    }

    return declaration.getName().equalsIgnoreCase(name);
  }

  private static <T extends DelphiNode> T unwrap(ExpressionNode node, Class<T> unwrapClass) {
    node = node.skipParentheses();
    if (node instanceof PrimaryExpressionNode && node.getChildren().size() == 1) {
      DelphiNode child = node.getChild(0);
      if (unwrapClass.isAssignableFrom(child.getClass())) {
        return unwrapClass.cast(node.getChildren().get(0));
      }
    }
    return null;
  }
}
