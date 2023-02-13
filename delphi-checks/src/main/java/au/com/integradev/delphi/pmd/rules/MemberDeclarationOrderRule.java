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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.node.FieldDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.FieldSectionNode;
import au.com.integradev.delphi.antlr.ast.node.MethodDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.PropertyNode;
import au.com.integradev.delphi.antlr.ast.node.VisibilitySectionNode;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import au.com.integradev.delphi.antlr.ast.node.Node;

public class MemberDeclarationOrderRule extends AbstractDelphiRule {
  private enum BodySegment {
    FIELDS,
    METHODS,
    PROPERTIES
  }

  private List<Node> getOutOfOrderDeclarations(VisibilitySectionNode sectionNode) {
    var currentSegment = BodySegment.FIELDS;
    List<Node> outOfOrderDeclarations = new ArrayList<>();

    for (int i = 0; i < sectionNode.jjtGetNumChildren(); i++) {
      DelphiNode itemNode = sectionNode.jjtGetChild(i);

      if (itemNode instanceof FieldSectionNode && currentSegment != BodySegment.FIELDS) {
        outOfOrderDeclarations.addAll(itemNode.findChildrenOfType(FieldDeclarationNode.class));
      } else if (itemNode instanceof MethodDeclarationNode
          && currentSegment != BodySegment.METHODS) {
        if (currentSegment == BodySegment.FIELDS) {
          currentSegment = BodySegment.METHODS;
        } else {
          outOfOrderDeclarations.add(itemNode);
        }
      } else if (itemNode instanceof PropertyNode) {
        // A property declaration has no restrictions on it, but it invalidates any later fields
        // or methods.
        currentSegment = BodySegment.PROPERTIES;
      }
    }

    return outOfOrderDeclarations;
  }

  @Override
  public RuleContext visit(VisibilitySectionNode sectionNode, RuleContext data) {
    List<Node> outOfOrderDeclarations = getOutOfOrderDeclarations(sectionNode);

    if (!outOfOrderDeclarations.isEmpty()) {
      addViolationWithMessage(
          data,
          outOfOrderDeclarations.get(0),
          "Reorder this visibility section ({0} declarations are out of order, starting here)",
          new Object[] {outOfOrderDeclarations.size()});
    }

    return super.visit(sectionNode, data);
  }
}
