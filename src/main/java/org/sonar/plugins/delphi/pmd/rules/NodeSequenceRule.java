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
 * Rule that checks, if sequence of nodes occur in file. Sequence should be lowercase, since it is not case sensitive.
 */
public class NodeSequenceRule extends DelphiRule {

  private String[] sequence;
  private int count;
  private DelphiPMDNode firstMatchNode;

  @Override
  public Object visit(DelphiPMDNode node, Object data) {

    if (node.getText().equalsIgnoreCase(sequence[count])) {
      if (++count == 1) {
        firstMatchNode = node; // save first match node
      } else if (count >= sequence.length) { // end the sequence
        addViolation(data, firstMatchNode);
        count = 0; // reset
      }
    } else {
      count = 0; // reset if we bumped out of the sequence
    }

    return data;
  }

  @Override
  protected void init() {
    count = 0;
    firstMatchNode = null;
    sequence = getStringProperty("sequence").split(",");
  }

}
