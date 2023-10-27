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
package au.com.integradev.delphi.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

@Rule(key = "IndexLastListElement")
public class IndexLastListElementCheck extends DelphiCheck {

  @Override
  public DelphiCheckContext visit(ArrayAccessorNode arrayTypeNode, DelphiCheckContext context) {
    doVisit(arrayTypeNode, context);

    return super.visit(arrayTypeNode, context);
  }

  private void doVisit(ArrayAccessorNode arrayTypeNode, DelphiCheckContext context) {
    List<ExpressionNode> expressions = arrayTypeNode.getExpressions();
    if (expressions.size() != 1) {
      return;
    }

    if (arrayTypeNode.getChildIndex() <= 0) {
      return;
    }

    ExpressionNode indexExpr = expressions.get(0);
    DelphiNode arrayVar = arrayTypeNode.getParent().getChild(arrayTypeNode.getChildIndex() - 1);

    Optional.ofNullable(indexExpr)
        .map(ExpressionNode::skipParentheses)
        .filter(BinaryExpressionNode.class::isInstance)
        .map(BinaryExpressionNode.class::cast)
        .filter(IndexLastListElementCheck::isSubOne)
        .flatMap(b -> getArrayPropertyReference(b.getLeft(), arrayVar.getImage()))
        .ifPresent(
            propertyName -> {
              if (propertyName.equalsIgnoreCase("System.Classes.TList.Count")) {
                reportIssueWithQuickFix(context, arrayTypeNode, "Use TList.Last.");
              } else if (propertyName.equalsIgnoreCase(
                  "System.Generics.Collections.TList<T>.Count")) {
                reportIssueWithQuickFix(context, arrayTypeNode, "Use TList<T>.Last.");
              }
            });
  }

  private static void reportIssueWithQuickFix(
      DelphiCheckContext context, ArrayAccessorNode node, String message) {
    context
        .newIssue()
        .onNode(node)
        .withMessage(message)
        .withQuickFixes(
            QuickFix.newFix("Replace with TList.Last")
                .withEdit(QuickFixEdit.replace(node, ".Last")))
        .report();
  }

  private static boolean isSubOne(BinaryExpressionNode binary) {
    return binary.getOperator() == BinaryOperator.SUBTRACT && isIntLiteral(binary.getRight(), 1);
  }

  private static Optional<String> getArrayPropertyReference(ExpressionNode node, String arrayVar) {
    node = node.skipParentheses();

    if (node.getChildren().size() != 1) {
      return Optional.empty();
    }
    return Optional.of(node.getChild(0))
        .filter(left -> left.getChildren().size() == 3)
        .filter(left -> sameIdentifier(left.getChild(0), arrayVar))
        .filter(left -> isDot(left.getChild(1)))
        .flatMap(left -> getPropertyReference(left.getChild(2)));
  }

  private static boolean isIntLiteral(ExpressionNode node, int i) {
    return Optional.ofNullable(ExpressionNodeUtils.unwrapInteger(node))
        .filter(literal -> literal.getValue().intValue() == i)
        .isPresent();
  }

  private static Optional<String> getPropertyReference(DelphiNode child) {
    return Optional.of(child)
        .filter(NameReferenceNode.class::isInstance)
        .map(NameReferenceNode.class::cast)
        .map(NameReferenceNode::getNameDeclaration)
        .filter(PropertyNameDeclaration.class::isInstance)
        .map(PropertyNameDeclaration.class::cast)
        .map(PropertyNameDeclaration::fullyQualifiedName);
  }

  private static boolean isDot(DelphiNode child) {
    return child instanceof CommonDelphiNode && child.getToken().getType() == DelphiTokenType.DOT;
  }

  private static boolean sameIdentifier(DelphiNode child, String arrayVar) {
    return child instanceof IdentifierNode && child.getImage().equals(arrayVar);
  }
}
