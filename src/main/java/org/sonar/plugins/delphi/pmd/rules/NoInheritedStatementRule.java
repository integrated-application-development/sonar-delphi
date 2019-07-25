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
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * Class that checks if 'inherited' statement is in some function or procedure. If no, it triggers a
 * violation.
 */
public abstract class NoInheritedStatementRule extends DelphiRule {

  private static final int MAX_LOOK_AHEAD = 3;
  private String lookFor = "";

  public void setLookFor(String lookFor) {
    this.lookFor = lookFor;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (StringUtils.isEmpty(lookFor) || !node.getText().equalsIgnoreCase(lookFor)) {
      return;
    }

    Tree beginNode = findBeginNode(node);
    if (beginNode == null) {
      return;
    }

    boolean wasInherited = false;
    for (int c = 0; c < beginNode.getChildCount(); c++) {
      if (beginNode.getChild(c).getType() == DelphiLexer.INHERITED) {
        wasInherited = true;
        break;
      }
    }

    if (!wasInherited && shouldAddRule(node)) {
      addViolation(ctx, node);
    }
  }

  private Tree findBeginNode(DelphiPMDNode node) {
    Tree parent = node.getParent();
    int childIndex = node.getChildIndex();
    int childCount = parent.getChildCount();

    for (int i = childIndex + 1; i < childIndex + MAX_LOOK_AHEAD && i < childCount; ++i) {
      if (parent.getChild(i).getType() == DelphiLexer.BEGIN) {
        return parent.getChild(i);
      }
    }

    return null;
  }

  protected abstract boolean shouldAddRule(DelphiPMDNode node);

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
