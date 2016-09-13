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
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule that counts the number of specified nodes, and if the count exceeds the
 * specified limit, rule will produce a violation
 */
public class CountRule extends DelphiRule {

  private String stringToSearch;
  private int typeToSearch;
  protected int limit;
  protected int count;
  /**
   * Number to increase the count.
   */
  protected int strength = 1;
  /**
   * Should we reset the counter after exceeding the limit.
   */
  protected boolean reset = true;

  public String getStringToSearch() {
    return stringToSearch;
  }

  public void setStringToSearch(String stringToSearch) {
    this.stringToSearch = stringToSearch;
  }

  public void setTypeToSearch(int typeToSearch) {
    this.typeToSearch = typeToSearch;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (!shouldCount(node)) {
      return;
    }

    increaseCounter(strength);

    if (exceedsLimit()) {
      addViolation(ctx, node, getMessage());
      if (reset) {
        count = 0;
      }
    }
  }

  protected boolean shouldCount(DelphiPMDNode node) {
    return matchesText(node) || matchesType(node);
  }

  protected boolean matchesText(DelphiPMDNode node) {
    return node.getText().equals(stringToSearch);
  }

  protected boolean matchesType(DelphiPMDNode node) {
    return node.getType() == typeToSearch;
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
    limit = getIntProperty(LIMIT);
  }

}
