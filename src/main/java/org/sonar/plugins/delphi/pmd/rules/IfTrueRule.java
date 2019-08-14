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
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;

/** Checks for rule violation: if x = true then (redundant) */
public class IfTrueRule extends BlockCounterRule {

  private boolean wasEquals;

  @Override
  public void start(RuleContext ctx) {
    super.start(ctx);
    wasEquals = false;
    setStringToSearch("true");
  }

  @Override
  protected boolean accept(DelphiNode node) {
    if (!wasEquals && "=".equals(node.getText())) {
      wasEquals = true;
    } else if (wasEquals && node.getText().equals(getStringToSearch())) {
      // save this node as violation
      firstNode = node;
      return true;
    } else {
      // reset if 'true' is not directly after '='
      wasEquals = false;
    }

    return false;
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
