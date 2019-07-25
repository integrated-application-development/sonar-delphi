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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * Class for checking if we are using .Free with checking if variable is assigned (redundant): if
 * assigned(x) then x.free; if x &lt;&gt; nil then x.free;
 */
public class AssignedAndFreeRule extends DelphiRule {

  private enum AssignCheckType {
    NOT_APPLICABLE,
    ASSIGNED,
    NIL_COMPARE,
    NIL_COMPARE_BACKWARDS
  }

  private String variableName;
  private AssignCheckType assignCheckType;

  @Override
  protected void init() {
    variableName = "";
    assignCheckType = AssignCheckType.NOT_APPLICABLE;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    assignCheckType = findAssignCheckType(node);
    variableName = getVariableName(node);

    if (variableName.isEmpty()) {
      return;
    }

    DelphiPMDNode violationNode = findViolationNode(node);

    if (violationNode == null) {
      return;
    }

    addViolation(ctx, violationNode);
  }

  private AssignCheckType findAssignCheckType(DelphiPMDNode node) {
    int type = node.getType();

    if (type == DelphiLexer.NIL) {
      DelphiPMDNode prevNode = node.prevNode();
      if (prevNode != null && prevNode.getType() == DelphiLexer.NOT_EQUAL) {
        return AssignCheckType.NIL_COMPARE;
      }

      DelphiPMDNode nextNode = node.nextNode();
      if (nextNode != null && nextNode.getType() == DelphiLexer.NOT_EQUAL) {
        return AssignCheckType.NIL_COMPARE_BACKWARDS;
      }
    }

    if (type == DelphiLexer.TkIdentifier && node.getText().equalsIgnoreCase("Assigned")) {
      return AssignCheckType.ASSIGNED;
    }

    return AssignCheckType.NOT_APPLICABLE;
  }

  private String getVariableName(DelphiPMDNode node) {
    DelphiPMDNode identStart;

    switch (assignCheckType) {
      case ASSIGNED:
      case NIL_COMPARE_BACKWARDS:
        // Jump forward two nodes and get the variable name
        identStart = node.nextNode().nextNode();
        return getQualifiedIdent(identStart);

      case NIL_COMPARE:
        // Jump back two nodes and get the variable name in reverse
        identStart = node.prevNode().prevNode();
        return getQualifiedIdentInReverse(identStart);

      default:
        // Do nothing
    }

    return "";
  }

  private String getQualifiedIdent(DelphiPMDNode node) {
    StringBuilder nameBuilder = new StringBuilder();
    DelphiPMDNode currentNode = node;

    while (isInsideQualifiedIdent(currentNode)) {
      nameBuilder.append(currentNode.getText());
      currentNode = currentNode.nextNode();
    }

    return nameBuilder.toString();
  }

  private String getQualifiedIdentInReverse(DelphiPMDNode node) {
    StringBuilder nameBuilder = new StringBuilder();
    DelphiPMDNode currentNode = node;

    while (isInsideQualifiedIdent(currentNode)) {
      nameBuilder.insert(0, currentNode.getText());
      currentNode = currentNode.prevNode();
    }

    return nameBuilder.toString();
  }

  private boolean isInsideQualifiedIdent(DelphiPMDNode node) {
    if (node == null) {
      return false;
    }

    return node.getType() == DelphiLexer.TkIdentifier || node.getType() == DelphiLexer.DOT;
  }

  private DelphiPMDNode findViolationNode(DelphiPMDNode node) {
    if (hasConditionsAfterAssignCheck(node)) {
      // This caters to cases where the assignment check is reasonably used as a short-circuit
      // Example: "if Assigned(X) and X.ShouldBeFreed then X.Free;"
      return null;
    }

    DelphiPMDNode thenNode = node.findNextSiblingOfType(DelphiLexer.THEN);

    if (thenNode == null) {
      return null;
    }

    DelphiPMDNode startNode = thenNode.nextNode();

    if (startNode.getType() == DelphiLexer.BEGIN) {
      startNode = (DelphiPMDNode) startNode.getChild(0);
    }

    return findViolationNodeInStatement(startNode);
  }

  private boolean hasConditionsAfterAssignCheck(DelphiPMDNode node) {
    Tree parent = node.getParent();

    if (parent != null) {
      for (int i = node.getChildIndex(); i < parent.getChildCount(); ++i) {
        int type = parent.getChild(i).getType();

        if (type == DelphiLexer.OR || type == DelphiLexer.AND) {
          return true;
        }

        if (type == DelphiLexer.THEN) {
          return false;
        }
      }
    }

    return false;
  }

  private DelphiPMDNode findViolationNodeInStatement(DelphiPMDNode node) {
    if (node.getType() == DelphiLexer.END) {
      return null;
    }

    DelphiPMDNode violationNode = findFreeViolationNode(node);

    if (violationNode == null) {
      violationNode = findFreeAndNilViolationNode(node);
    }

    return violationNode;
  }

  private DelphiPMDNode findFreeViolationNode(DelphiPMDNode node) {
    StringBuilder freedVariableName = new StringBuilder();
    DelphiPMDNode currentNode = node;

    while (isInsideQualifiedIdent(currentNode)) {
      DelphiPMDNode nextNode = currentNode.nextNode();
      if (isFreeViolation(currentNode, nextNode, freedVariableName)) {
        return nextNode;
      }

      freedVariableName.append(currentNode.getText());
      currentNode = nextNode;
    }

    return null;
  }

  private boolean isFreeViolation(DelphiPMDNode current, DelphiPMDNode next, StringBuilder name) {
    return current.getType() == DelphiLexer.DOT
        && next.getText().equalsIgnoreCase("Free")
        && name.toString().equalsIgnoreCase(variableName);
  }

  private DelphiPMDNode findFreeAndNilViolationNode(DelphiPMDNode node) {
    if (node.getText().equalsIgnoreCase("FreeAndNil")) {
      String freedVariableName = getQualifiedIdent(node.nextNode().nextNode());

      if (freedVariableName.equalsIgnoreCase(variableName)) {
        return node;
      }
    }

    return null;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
