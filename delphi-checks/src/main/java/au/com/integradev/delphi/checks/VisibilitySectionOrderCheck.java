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
package au.com.integradev.delphi.checks;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "VisibilitySectionOrderRule", repositoryKey = "delph")
@Rule(key = "VisibilitySectionOrder")
public class VisibilitySectionOrderCheck extends DelphiCheck {
  private static final String MESSAGE = "Move this visibility section.";

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

  @Override
  public DelphiCheckContext visit(StructTypeNode structTypeNode, DelphiCheckContext context) {
    checkOrder(structTypeNode.getVisibilitySections(), context);
    return super.visit(structTypeNode, context);
  }

  private void checkOrder(
      List<VisibilitySectionNode> visibilitySections, DelphiCheckContext context) {
    int currentVisibilityOrder = VISIBILITY_ORDER.get(VisibilityType.IMPLICIT_PUBLISHED);

    for (var visibilitySection : visibilitySections) {
      int sectionVisibilityOrder = VISIBILITY_ORDER.get(visibilitySection.getVisibility());

      if (sectionVisibilityOrder >= currentVisibilityOrder) {
        currentVisibilityOrder = sectionVisibilityOrder;
      } else {
        var visibilityNode = getVisibilityNode(visibilitySection);
        reportIssue(
            context, Objects.requireNonNullElse(visibilityNode, visibilitySection), MESSAGE);
      }
    }
  }

  private static VisibilityNode getVisibilityNode(VisibilitySectionNode visibilitySectionNode) {
    Node firstChild = visibilitySectionNode.getChild(0);
    if (firstChild instanceof VisibilityNode) {
      return (VisibilityNode) firstChild;
    }
    return null;
  }
}
