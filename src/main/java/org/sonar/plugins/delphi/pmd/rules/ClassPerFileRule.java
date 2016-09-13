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
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.ArrayList;
import java.util.List;

/**
 * It counts how many classes there are in one file.
 * 
 * @author "Fabricio Colombo"
 * @since 0.3
 */
public class ClassPerFileRule extends CountRule {

  /**
   * Store the visited inner classes to to ignore the count.
   */
  private List<Tree> visitedInnerClasses;

  @Override
  protected void init() {
    super.init();
    reset = false;
    setTypeToSearch(DelphiLexer.TkClass);
    visitedInnerClasses = new ArrayList<>();
  }

  @Override
  public String getMessage() {
    return String.format("File has too many classes, maximum number of classes is %d.", limit);
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (!shouldCount(node)) {
      return;
    }

    if (visitedInnerClasses.contains(node)) {
      return;
    }

    visitedInnerClasses.addAll(findInnerClasses(node));

    increaseCounter(strength);

    if (exceedsLimit()) {
      addViolation(ctx, node, getMessage());
      if (reset) {
        count = 0;
      }
    }
  }

  private List<Tree> findInnerClasses(DelphiPMDNode node) {
    return node.findAllChildren(DelphiLexer.TkClass);
  }
}
