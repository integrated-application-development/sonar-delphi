/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ConstructorWithoutInheritedStatementRule extends NoInheritedStatementRule {

  private List<String> knewRecords = new ArrayList<>();

  @Override
  protected void init() {
    super.init();
    setLookFor("constructor");
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.TkRecord) {
      knewRecords.add(node.getParent().getText());
    }
    super.visit(node, ctx);
  }

  @Override
  protected boolean shouldAddRule(DelphiPMDNode node) {
    if (node.getChild(0).getType() == DelphiLexer.TkFunctionName) {
      String typeName = node.getChild(0).getChild(0).getText();
      return !knewRecords.contains(typeName);
    }
    return super.shouldAddRule(node);
  }
}
