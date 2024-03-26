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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "PlatformDependentTruncationRule", repositoryKey = "delph")
@Rule(key = "PlatformDependentTruncation")
public class PlatformDependentTruncationCheck extends DelphiCheck {
  private static final String MESSAGE =
      "This integer value may or may not be truncated depending on the target platform.";

  @Override
  public DelphiCheckContext visit(AssignmentStatementNode assignment, DelphiCheckContext context) {
    if (isViolation(assignment.getValue().getType(), assignment.getAssignee().getType())) {
      reportIssue(context, assignment, MESSAGE);
    }
    return super.visit(assignment, context);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    Node previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (!(previous instanceof NameReferenceNode)) {
      return super.visit(argumentList, context);
    }

    ProceduralType procedural = getProceduralType((NameReferenceNode) previous);
    if (procedural == null) {
      return super.visit(argumentList, context);
    }

    List<ArgumentNode> arguments = argumentList.getArgumentNodes();
    List<Parameter> parameters = procedural.parameters();
    for (int i = 0; i < arguments.size() && i < parameters.size(); ++i) {
      ExpressionNode argument = arguments.get(i).getExpression();
      if (isViolation(argument.getType(), parameters.get(i).getType())) {
        reportIssue(context, argument, MESSAGE);
      }
    }
    return super.visit(argumentList, context);
  }

  private static ProceduralType getProceduralType(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof TypedDeclaration) {
      Type type = ((TypedDeclaration) declaration).getType();
      if (type instanceof ProceduralType) {
        return (ProceduralType) type;
      }
    }
    return null;
  }

  private static boolean isViolation(Type from, Type to) {
    if (!from.isInteger() || !to.isInteger()) {
      return false;
    }

    if (isNativeInteger(from) == isNativeInteger(to)) {
      return false;
    }

    return (isNativeInteger(from) && to.size() < 8) || (isNativeInteger(to) && from.size() > 4);
  }

  private static boolean isNativeInteger(Type type) {
    while (true) {
      if (type.is(IntrinsicType.NATIVEINT) || type.is(IntrinsicType.NATIVEUINT)) {
        return true;
      }

      if (type.isAlias()) {
        type = ((Type.AliasType) type).aliasedType();
      } else {
        break;
      }
    }
    return false;
  }
}
