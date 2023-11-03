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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "IfThenShortCircuitRule", repositoryKey = "delph")
@Rule(key = "IfThenShortCircuit")
public class IfThenShortCircuitCheck extends DelphiCheck {
  private static final String MESSAGE = "Change this unsafe IfThen call to an 'if' statement.";

  @Override
  public DelphiCheckContext visit(NameReferenceNode nameReference, DelphiCheckContext context) {
    DelphiNode parent = nameReference.getParent();
    if (parent instanceof PrimaryExpressionNode
        && nameReference.getLastName().getIdentifier().getImage().equalsIgnoreCase("IfThen")) {
      DelphiNode argumentList = parent.getChild(nameReference.getChildIndex() + 1);
      if (argumentList instanceof ArgumentListNode) {
        List<ExpressionNode> arguments = ((ArgumentListNode) argumentList).getArguments();
        if (isViolation(arguments)) {
          reportIssue(context, nameReference, MESSAGE);
        }
      }
    }
    return super.visit(nameReference, context);
  }

  private static boolean isViolation(List<ExpressionNode> arguments) {
    if (arguments.size() == 3) {
      for (String image : findImagesCheckedForAssignment(arguments.get(0))) {
        if (isAccessedBy(image, arguments.get(1)) || isAccessedBy(image, arguments.get(2))) {
          return true;
        }
      }
    }
    return false;
  }

  private static Set<String> findImagesCheckedForAssignment(ExpressionNode expression) {
    Set<String> images = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    images.addAll(findImagesInPrimaryExpressions(expression));
    images.addAll(findImagesInBinaryExpressions(expression));
    return images;
  }

  private static Set<String> findImagesInPrimaryExpressions(ExpressionNode expression) {
    Set<String> images = new HashSet<>();
    for (var binaryExpression : findDescendentsOrSelf(expression, BinaryExpressionNode.class)) {
      if (binaryExpression.getOperator() == BinaryOperator.EQUAL
          || binaryExpression.getOperator() == BinaryOperator.NOT_EQUAL) {
        ExpressionNode left = binaryExpression.getLeft();
        ExpressionNode right = binaryExpression.getRight();
        if (ExpressionNodeUtils.isNilLiteral(left)) {
          images.add(right.getImage());
        } else if (ExpressionNodeUtils.isNilLiteral(right)) {
          images.add(left.getImage());
        }
      }
    }
    return images;
  }

  private static Set<String> findImagesInBinaryExpressions(ExpressionNode expression) {
    Set<String> images = new HashSet<>();
    for (var primaryExpression : findDescendentsOrSelf(expression, PrimaryExpressionNode.class)) {
      Node nameReference = primaryExpression.getChild(0);
      Node argumentList = primaryExpression.getChild(1);
      if (nameReference instanceof NameReferenceNode && argumentList instanceof ArgumentListNode) {
        NameDeclaration declaration =
            ((NameReferenceNode) nameReference).getLastName().getNameDeclaration();
        if (declaration instanceof MethodNameDeclaration
            && ((MethodNameDeclaration) declaration)
                .fullyQualifiedName()
                .equals("System.Assigned")) {
          ExpressionNode argument = ((ArgumentListNode) argumentList).getArguments().get(0);
          images.add(argument.getImage());
        }
      }
    }
    return images;
  }

  private static boolean isAccessedBy(String image, ExpressionNode expression) {
    return findDescendentsOrSelf(expression, PrimaryExpressionNode.class).stream()
        .map(Node::getImage)
        .filter(primaryImage -> primaryImage.length() > image.length())
        .anyMatch(
            primaryImage -> {
              char nextChar = primaryImage.charAt(image.length());
              return (nextChar == '.' || nextChar == '^')
                  && StringUtils.startsWithIgnoreCase(primaryImage, image);
            });
  }

  private static <T extends Node> List<T> findDescendentsOrSelf(DelphiNode node, Class<T> clazz) {
    List<T> result = node.findDescendantsOfType(clazz);
    if (clazz.isInstance(node)) {
      result.add(clazz.cast(node));
    }
    return result;
  }
}
