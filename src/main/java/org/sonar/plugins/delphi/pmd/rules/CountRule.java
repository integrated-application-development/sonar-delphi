/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule that counts the number of specified nodes, and if the count exceeds the specified limit, rule will produce a violation
 */
public class CountRule extends DelphiRule {

  protected String str; // string to look for
  protected int limit; // limit
  protected int count; // number of hits
  protected int strength = 1; // number to increase the count
  protected boolean reset = true; // should we reset counter after exceeding the limit
  protected Object dataRef = null; // data reference

  @Override
  public Object visit(DelphiPMDNode node, Object data) {
    dataRef = data; // data reference
    if (shouldCount(node)) {
      increaseCounter(strength); // increase the counter
    }

    if (exceedsLimit()) { // if we exceeded the limit
      addViolation(data, node); // add a violation
      if (reset) {
        count = 0;
      }
    }
    return data;
  }

  protected boolean shouldCount(DelphiPMDNode node) {
    return node.getText().equals(str);
  }

  protected void increaseCounter(int howMuch) {
    count += howMuch;
  }

  protected boolean exceedsLimit() {
    return count > limit;
  }

  @Override
  protected void init() {
    count = 0;
    strength = 1;
    str = getStringProperty("search");
    limit = Integer.valueOf(getStringProperty("limit"));
  }
}
