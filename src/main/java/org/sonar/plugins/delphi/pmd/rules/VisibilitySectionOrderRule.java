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

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.StructTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType;
import org.sonar.plugins.delphi.antlr.ast.node.VisibilityNode;
import org.sonar.plugins.delphi.antlr.ast.node.VisibilitySectionNode;

public class VisibilitySectionOrderRule extends AbstractDelphiRule {
  private static final Map<VisibilityType, Integer> VISIBILITY_ORDER =
      Maps.immutableEnumMap(
          Map.of(
              VisibilityType.IMPLICIT_PUBLISHED, 0,
              VisibilityType.STRICT_PRIVATE, 1,
              VisibilityType.PRIVATE, 2,
              VisibilityType.STRICT_PROTECTED, 3,
              VisibilityType.PROTECTED, 4,
              VisibilityType.PUBLIC, 5,
              VisibilityType.PUBLISHED, 6));

  private VisibilityNode getVisibilityNode(VisibilitySectionNode visibilitySectionNode) {
    if (visibilitySectionNode.jjtGetNumChildren() > 0) {
      Node firstChild = visibilitySectionNode.jjtGetChild(0);
      if (firstChild instanceof VisibilityNode) {
        return (VisibilityNode) firstChild;
      }
    }

    return null;
  }

  private void checkOrder(List<VisibilitySectionNode> visibilitySections, RuleContext data) {
    int currentVisibilityOrder = VISIBILITY_ORDER.get(VisibilityType.IMPLICIT_PUBLISHED);

    for (var visibilitySection : visibilitySections) {
      int sectionVisibilityOrder = VISIBILITY_ORDER.get(visibilitySection.getVisibility());

      if (sectionVisibilityOrder >= currentVisibilityOrder) {
        currentVisibilityOrder = sectionVisibilityOrder;
      } else {
        var visibilityNode = getVisibilityNode(visibilitySection);
        addViolation(data, Objects.requireNonNullElse(visibilityNode, visibilitySection));
      }
    }
  }

  @Override
  public RuleContext visit(StructTypeNode structTypeNode, RuleContext data) {
    checkOrder(structTypeNode.getVisibilitySections(), data);
    return super.visit(structTypeNode, data);
  }
}
