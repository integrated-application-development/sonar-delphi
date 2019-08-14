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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * It counts how many classes there are in one file.
 *
 * @author "Fabricio Colombo"
 * @since 0.3
 */
public class ClassPerFileRule extends CountRule {

  /** Store the visited inner classes to to ignore the count. */
  private Deque<Tree> visitedInnerClasses;

  @Override
  public void start(RuleContext ctx) {
    super.start(ctx);
    reset = false;
    setTypeToSearch(DelphiLexer.TkClass);
    visitedInnerClasses = new ArrayDeque<>();
  }

  @Override
  public String getMessage() {
    return String.format(
        "File has too many classes, maximum number of classes is %d.", definedLimit);
  }

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
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

  private List<Tree> findInnerClasses(DelphiNode node) {
    return node.findAllChildren(DelphiLexer.TkClass);
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
