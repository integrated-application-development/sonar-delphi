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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MemberDeclarationOrderRule", repositoryKey = "delph")
@Rule(key = "MemberDeclarationOrder")
public class MemberDeclarationOrderCheck extends DelphiCheck {
  private enum BodySegment {
    FIELDS,
    ROUTINES,
    PROPERTIES
  }

  @Override
  public DelphiCheckContext visit(VisibilitySectionNode sectionNode, DelphiCheckContext context) {
    List<DelphiNode> outOfOrderDeclarations = getOutOfOrderDeclarations(sectionNode);

    if (!outOfOrderDeclarations.isEmpty()) {
      reportIssue(
          context,
          outOfOrderDeclarations.get(0),
          String.format(
              "Reorder this visibility section (%d declarations are out of order, starting here)",
              outOfOrderDeclarations.size()));
    }

    return super.visit(sectionNode, context);
  }

  private static List<DelphiNode> getOutOfOrderDeclarations(VisibilitySectionNode sectionNode) {
    var currentSegment = BodySegment.FIELDS;
    List<DelphiNode> outOfOrderDeclarations = new ArrayList<>();

    for (DelphiNode itemNode : sectionNode.getChildren()) {
      if (itemNode instanceof FieldSectionNode && currentSegment != BodySegment.FIELDS) {
        outOfOrderDeclarations.addAll(itemNode.findChildrenOfType(FieldDeclarationNode.class));
      } else if (itemNode instanceof RoutineDeclarationNode
          && currentSegment != BodySegment.ROUTINES) {
        if (currentSegment == BodySegment.FIELDS) {
          currentSegment = BodySegment.ROUTINES;
        } else {
          outOfOrderDeclarations.add(itemNode);
        }
      } else if (itemNode instanceof PropertyNode) {
        // A property declaration has no restrictions on it, but it invalidates any later fields
        // or routines.
        currentSegment = BodySegment.PROPERTIES;
      }
    }

    return outOfOrderDeclarations;
  }
}
