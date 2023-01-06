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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.type.parameter.Parameter;

public class PlatformDependentTruncationRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(AssignmentStatementNode assignment, RuleContext data) {
    if (isViolation(assignment.getValue().getType(), assignment.getAssignee().getType())) {
      addViolation(data, assignment);
    }
    return super.visit(assignment, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode argumentList, RuleContext data) {
    Node previous = argumentList.jjtGetParent().jjtGetChild(argumentList.jjtGetChildIndex() - 1);
    if (!(previous instanceof NameReferenceNode)) {
      return super.visit(argumentList, data);
    }

    ProceduralType procedural = getProceduralType((NameReferenceNode) previous);
    if (procedural == null) {
      return super.visit(argumentList, data);
    }

    List<ExpressionNode> arguments = argumentList.getArguments();
    List<Parameter> parameters = procedural.parameters();
    for (int i = 0; i < arguments.size() && i < parameters.size(); ++i) {
      ExpressionNode argument = arguments.get(i);
      if (isViolation(argument.getType(), parameters.get(i).getType())) {
        addViolation(data, argument);
      }
    }
    return super.visit(argumentList, data);
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
    from = TypeUtils.findBaseType(from);
    to = TypeUtils.findBaseType(to);

    if (!from.isInteger() || !to.isInteger()) {
      return false;
    }

    if (isNativeInteger(from) == isNativeInteger(to)) {
      return false;
    }

    return (isNativeInteger(from) && to.size() < 8) || (isNativeInteger(to) && from.size() > 4);
  }

  private static boolean isNativeInteger(Type type) {
    return type.is(IntrinsicType.NATIVEINT) || type.is(IntrinsicType.NATIVEUINT);
  }
}
