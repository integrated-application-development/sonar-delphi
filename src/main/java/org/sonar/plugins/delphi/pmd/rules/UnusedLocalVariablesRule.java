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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationListNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;

public class UnusedLocalVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    Set<NameOccurrence> excludedOccurrences = new HashSet<>();
    for (var assignment : method.findDescendantsOfType(AssignmentStatementNode.class)) {
      ExpressionNode assigneee = assignment.getAssignee();
      if (assigneee instanceof PrimaryExpressionNode
          && assigneee.jjtGetNumChildren() == 1
          && assigneee.jjtGetChild(0) instanceof NameReferenceNode) {
        NameReferenceNode nameReference = (NameReferenceNode) assigneee.jjtGetChild(0);
        NameOccurrence occurrence = nameReference.getNameOccurrence();
        if (nameReference.nextName() == null && occurrence != null) {
          excludedOccurrences.add(occurrence);
        }
      }
    }

    List<NameDeclarationNode> declarations = new ArrayList<>();
    method.findDescendantsOfType(VarDeclarationNode.class).stream()
        .map(VarDeclarationNode::getNameDeclarationList)
        .map(NameDeclarationListNode::getDeclarations)
        .forEach(declarations::addAll);
    method.findDescendantsOfType(VarStatementNode.class).stream()
        .map(VarStatementNode::getNameDeclarationList)
        .map(NameDeclarationListNode::getDeclarations)
        .forEach(declarations::addAll);

    declarations.stream()
        .filter(node -> excludedOccurrences.containsAll(node.getUsages()))
        .forEach(node -> addViolation(data, node));

    return data;
  }
}
