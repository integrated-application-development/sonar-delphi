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

import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
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
    if (visibilitySections.size() < 2) {
      return;
    }

    Deque<VisibilitySectionNode> pastSections = new ArrayDeque<>();
    pastSections.add(visibilitySections.get(0));

    for (int i = 1; i < visibilitySections.size(); i++) {
      VisibilitySectionNode thisSection = visibilitySections.get(i);
      VisibilitySectionNode lastSection = pastSections.peek();

      int thisSectionVisibility = VISIBILITY_ORDER.get(thisSection.getVisibility());

      // If this section has a lower visibility than the last section...
      if (lastSection != null
          && VISIBILITY_ORDER.get(lastSection.getVisibility()) > thisSectionVisibility) {
        // Remove excluded sections until a non-excluded one is found
        while (lastSection != null && isExcludedSection(lastSection)) {
          pastSections.pop();
          lastSection = pastSections.peek();
        }

        // If the nearest non-excluded section is higher visibility, report an issue
        if (lastSection != null
            && VISIBILITY_ORDER.get(lastSection.getVisibility()) > thisSectionVisibility) {
          report(context, thisSection);
        }
      }

      pastSections.add(thisSection);
    }
  }

  private void report(DelphiCheckContext context, VisibilitySectionNode visibilitySection) {
    var visibilityNode = getVisibilityNode(visibilitySection);
    reportIssue(context, Objects.requireNonNullElse(visibilityNode, visibilitySection), MESSAGE);
  }

  private static boolean isExcludedSection(VisibilitySectionNode visibilitySection) {
    return visibilitySection.getChildren().stream()
        .anyMatch(VisibilitySectionOrderCheck::hasUsagesInsideType);
  }

  private static boolean hasUsagesInsideType(DelphiNode node) {
    StructTypeNode structNode = node.getFirstParentOfType(StructTypeNode.class);
    return getVisibilitySectionItemUsages(node).stream()
        .anyMatch(usage -> isInsideNode(structNode, usage.getLocation()));
  }

  private static boolean isInsideNode(StructTypeNode structNode, Node node) {
    if (!node.getScope()
        .getEnclosingScope(FileScope.class)
        .equals(structNode.getScope().getEnclosingScope(FileScope.class))) {
      return false;
    }

    boolean afterStart =
        structNode.getBeginLine() < node.getBeginLine()
            || (structNode.getBeginLine() == node.getBeginLine()
                && structNode.getBeginColumn() <= node.getBeginColumn());
    boolean beforeEnd =
        structNode.getEndLine() > node.getEndLine()
            || (structNode.getEndLine() == node.getEndLine()
                && structNode.getEndColumn() >= node.getEndColumn());

    return afterStart && beforeEnd;
  }

  private static List<NameOccurrence> getVisibilitySectionItemUsages(DelphiNode node) {
    if (node instanceof MethodDeclarationNode) {
      return ((MethodDeclarationNode) node).getMethodNameNode().getUsages();
    } else if (node instanceof FieldSectionNode) {
      return ((FieldSectionNode) node)
          .getDeclarations().stream()
              .flatMap(
                  declarationNode ->
                      declarationNode.getDeclarationList().getDeclarations().stream()
                          .map(NameDeclarationNode::getUsages)
                          .flatMap(Collection::stream))
              .collect(Collectors.toList());
    } else if (node instanceof ConstSectionNode) {
      return node.findChildrenOfType(ConstDeclarationNode.class).stream()
          .map(declarationNode -> declarationNode.getNameDeclarationNode().getUsages())
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else if (node instanceof TypeSectionNode) {
      return ((TypeSectionNode) node)
          .getDeclarations().stream()
              .map(typeDecl -> typeDecl.getTypeNameNode().getUsages())
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
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
