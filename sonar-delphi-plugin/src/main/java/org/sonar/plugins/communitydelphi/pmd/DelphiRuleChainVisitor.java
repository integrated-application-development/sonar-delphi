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
package org.sonar.plugins.communitydelphi.pmd;

import java.util.List;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRuleChainVisitor;
import net.sourceforge.pmd.lang.rule.XPathRule;
import org.sonar.plugins.communitydelphi.antlr.ast.DelphiAST;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.pmd.rules.DelphiRule;

/** Delphi pmd rule chain visitor */
public class DelphiRuleChainVisitor extends AbstractRuleChainVisitor {

  @Override
  protected void visit(Rule rule, Node node, RuleContext ctx) {
    if (rule instanceof XPathRule) {
      ((XPathRule) rule).evaluate(node, ctx);
    } else {
      ((DelphiNode) node).accept((DelphiRule) rule, ctx);
    }
  }

  @Override
  protected void indexNodes(List<Node> nodes, RuleContext ctx) {
    var delphiParserVisitor =
        new DelphiParserVisitor<RuleContext>() {
          @Override
          public RuleContext visit(DelphiNode node, RuleContext data) {
            indexNode(node);
            return DelphiParserVisitor.super.visit(node, data);
          }
        };

    for (final Node node : nodes) {
      delphiParserVisitor.visit((DelphiAST) node, ctx);
    }
  }
}
