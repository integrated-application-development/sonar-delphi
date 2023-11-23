/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ClassHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ObjectTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

@Rule(key = "ConsecutiveVisibilitySection")
public class ConsecutiveVisibilitySectionCheck extends AbstractConsecutiveSectionCheck {
  @Override
  protected String getSectionName() {
    return "visibility";
  }

  @Override
  protected boolean areViolatingSections(DelphiNode first, DelphiNode second) {
    if (!(first instanceof VisibilitySectionNode && second instanceof VisibilitySectionNode)) {
      return false;
    }

    VisibilitySectionNode firstSection = (VisibilitySectionNode) first;
    VisibilitySectionNode secondSection = (VisibilitySectionNode) second;

    // Sections with different visibilities are OK
    if (firstSection.getVisibility() != secondSection.getVisibility()) {
      return false;
    }

    Optional<DelphiNode> firstItem = getOnlyItem(firstSection);
    Optional<DelphiNode> secondItem = getOnlyItem(secondSection);

    if (firstItem.isPresent() && secondItem.isPresent()) {
      return !areValidConsecutiveItems(firstItem.get(), secondItem.get());
    } else if (firstItem.isPresent()) {
      return !isValidConsecutiveItemWithMultiItemSection(firstItem.get());
    } else if (secondItem.isPresent()) {
      return !isValidConsecutiveItemWithMultiItemSection(secondItem.get());
    } else {
      return true;
    }
  }

  private boolean isValidConsecutiveItemWithMultiItemSection(DelphiNode item) {
    return item instanceof TypeSectionNode
        || item instanceof ConstSectionNode
        || (item instanceof FieldSectionNode && ((FieldSectionNode) item).isClassFieldSection());
  }

  private boolean areValidConsecutiveItems(DelphiNode firstItem, DelphiNode secondItem) {
    return
    // Type -> Not type
    (firstItem instanceof TypeSectionNode && !(secondItem instanceof TypeSectionNode))
        // Not type -> Type
        || (!(firstItem instanceof TypeSectionNode) && secondItem instanceof TypeSectionNode)
        // Const -> Not const
        || (firstItem instanceof ConstSectionNode && !(secondItem instanceof ConstSectionNode))
        // Not const -> const
        || (!(firstItem instanceof ConstSectionNode) && secondItem instanceof ConstSectionNode)
        // Class var -> Not class var (or vice versa)
        || (firstItem instanceof FieldSectionNode
            && secondItem instanceof FieldSectionNode
            && ((FieldSectionNode) firstItem).isClassFieldSection()
                != ((FieldSectionNode) secondItem).isClassFieldSection());
  }

  private Optional<DelphiNode> getOnlyItem(VisibilitySectionNode visibilitySection) {
    List<DelphiNode> items =
        visibilitySection.getChildren().stream()
            .filter(x -> !(x instanceof VisibilityNode))
            .collect(Collectors.toList());

    if (items.size() == 1) {
      return Optional.of(items.get(0));
    } else {
      return Optional.empty();
    }
  }

  @Override
  protected FilePosition getViolatingFilePosition(DelphiNode node) {
    // Visibility section keyword
    return FilePosition.from(node.getChild(0));
  }

  @Override
  public DelphiCheckContext visit(ClassTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(ObjectTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(RecordTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(InterfaceTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(ClassHelperTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(RecordHelperTypeNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }
}
