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

import java.util.ArrayDeque;
import java.util.Deque;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class ConstructorWithoutInheritedStatementRule extends NoInheritedStatementRule {

  private final Deque<String> recordTypes = new ArrayDeque<>();

  @Override
  protected void init() {
    recordTypes.clear();
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    handleNewTypes(node);

    if (node.getType() == DelphiLexer.CONSTRUCTOR) {
      if (isRecordConstructor(node) || isClassMethod(node)) {
        return;
      }

      checkViolation(ctx, node);
    }
  }

  private void handleNewTypes(DelphiPMDNode node) {
    if (node.getType() == DelphiLexer.TkNewType && isRecordType(node)) {
      recordTypes.add(getTypeName(node));
    }
  }

  private boolean isRecordConstructor(DelphiPMDNode node) {
    if (node.getChild(0).getType() == DelphiLexer.TkFunctionName) {
      String typeName = node.getChild(0).getChild(0).getText();
      return recordTypes.contains(typeName);
    }
    return false;
  }

  private boolean isRecordType(DelphiPMDNode newTypeNode) {
    Tree typeDeclNode = newTypeNode.getFirstChildWithType(DelphiLexer.TkNewTypeDecl);
    int type = typeDeclNode.getChild(0).getType();

    return type == DelphiLexer.TkRecord;
  }

  private String getTypeName(DelphiPMDNode newTypeNode) {
    Tree typeNameNode = newTypeNode.getFirstChildWithType(DelphiLexer.TkNewTypeName);
    return typeNameNode.getChild(0).getText();
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
