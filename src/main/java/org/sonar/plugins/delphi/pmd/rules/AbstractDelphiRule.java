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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.pmd.DelphiLanguageModule;

/** Basic rule class, extend this class to make your own rules. Do NOT extend from AbstractRule. */
public abstract class AbstractDelphiRule extends AbstractRule implements DelphiRule {
  private Set<Integer> suppressions = new HashSet<>();

  protected AbstractDelphiRule() {
    setLanguage(LanguageRegistry.getLanguage(DelphiLanguageModule.LANGUAGE_NAME));
    defineBaseProperties();
  }

  @Override
  public RuleContext visit(DelphiNode node, RuleContext data) {
    node.childrenAccept(this, data);
    return data;
  }

  @Override
  public void apply(List<? extends Node> acus, RuleContext ctx) {
    for (Node acu : acus) {
      DelphiAST ast = (DelphiAST) acu;
      updateSuppressions(ast);
      visit(ast, ctx);
    }
  }

  private void updateSuppressions(DelphiAST ast) {
    suppressions = ast.getSuppressions();
  }

  @Override
  public boolean isSuppressedLine(int line) {
    return suppressions.contains(line);
  }
}
