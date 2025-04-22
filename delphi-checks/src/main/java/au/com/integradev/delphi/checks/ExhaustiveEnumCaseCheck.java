/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.EnumElementNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.EnumType;

@Rule(key = "ExhaustiveEnumCase")
public class ExhaustiveEnumCaseCheck extends DelphiCheck {

  @Override
  public DelphiCheckContext visit(CaseStatementNode node, DelphiCheckContext context) {
    if (node.getElseBlockNode() == null) {
      EnumType enumType = getSelectorExpressionType(node);
      if (enumType != null) {
        Set<EnumElementNameDeclaration> enumElements = getEnumElements(enumType);

        List<ExpressionNode> expressions =
            node.getCaseItems().stream()
                .map(CaseItemStatementNode::getExpressions)
                .flatMap(List::stream)
                .map(ExpressionNode::skipParentheses)
                .collect(Collectors.toList());

        if (expressions.stream().allMatch(PrimaryExpressionNode.class::isInstance)) {
          expressions.stream()
              .map(this::getElementNameDeclaration)
              .filter(Objects::nonNull)
              .forEach(enumElements::remove);
        } else {
          // If there are more complex expressions (e.g. subrange), with the information we have
          // we can't determine if all elements are handled.
          enumElements.clear();
        }

        if (!enumElements.isEmpty()) {
          context
              .newIssue()
              .onFilePosition(FilePosition.from(node.getToken()))
              .withMessage(
                  String.format(
                      "Make this case statement exhaustive (%d unhandled value%s)",
                      enumElements.size(), enumElements.size() == 1 ? "" : "s"))
              .report();
        }
      }
    }

    return super.visit(node, context);
  }

  private NameDeclaration unpackExpressionDeclaration(ExpressionNode expression) {
    expression = expression.skipParentheses();
    if (!(expression instanceof PrimaryExpressionNode)) {
      return null;
    }

    DelphiNode maybeNameReference = expression.getChild(0);
    if (!(maybeNameReference instanceof NameReferenceNode)) {
      return null;
    }

    NameReferenceNode nameReference = ((NameReferenceNode) maybeNameReference).getLastName();
    return nameReference.getNameDeclaration();
  }

  private EnumElementNameDeclaration getElementNameDeclaration(ExpressionNode expression) {
    var declaration = unpackExpressionDeclaration(expression);
    if (declaration instanceof EnumElementNameDeclaration) {
      return (EnumElementNameDeclaration) declaration;
    } else {
      return null;
    }
  }

  private EnumType getSelectorExpressionType(CaseStatementNode node) {
    var declaration = unpackExpressionDeclaration(node.getSelectorExpression());
    Type type;

    if (declaration instanceof Invocable) {
      var invocable = (Invocable) declaration;
      if (invocable.getRequiredParametersCount() != 0) {
        return null;
      }

      type = invocable.getReturnType();
    } else if (declaration instanceof TypedDeclaration) {
      type = ((TypedDeclaration) declaration).getType();
    } else {
      return null;
    }

    if (type == null || !type.isEnum()) {
      return null;
    }

    return (EnumType) type;
  }

  private Set<EnumElementNameDeclaration> getEnumElements(EnumType enumType) {
    return enumType.typeScope().getAllDeclarations().stream()
        .filter(EnumElementNameDeclaration.class::isInstance)
        .map(EnumElementNameDeclaration.class::cast)
        .collect(Collectors.toSet());
  }
}
