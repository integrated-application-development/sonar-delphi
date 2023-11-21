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
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

public abstract class AbstractConsecutiveSectionCheck extends DelphiCheck {
  protected abstract boolean areViolatingSections(DelphiNode first, DelphiNode second);

  protected FilePosition getViolatingFilePosition(DelphiNode node) {
    return FilePosition.from(node.getToken());
  }

  protected abstract String getSectionName();

  public void checkViolation(DelphiNode parentNode, DelphiCheckContext context) {
    List<DelphiNode> children = parentNode.getChildren();

    for (int i = 1; i < children.size(); i++) {
      DelphiNode prevNode = children.get(i - 1);
      DelphiNode thisNode = children.get(i);
      if (areViolatingSections(prevNode, thisNode)) {
        context
            .newIssue()
            .onFilePosition(getViolatingFilePosition(thisNode))
            .withMessage(
                String.format("Merge this %s section with the previous section.", getSectionName()))
            .report();
      }
    }
  }
}
