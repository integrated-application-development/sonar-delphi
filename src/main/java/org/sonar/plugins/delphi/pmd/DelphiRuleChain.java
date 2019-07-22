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
package org.sonar.plugins.delphi.pmd;

import java.util.List;
import net.sourceforge.pmd.RuleChain;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * Delphi pmd rule chain
 */
public class DelphiRuleChain extends RuleChain {

  private final DelphiRuleChainVisitor visitor = new DelphiRuleChainVisitor();

  @Override
  public void apply(List<Node> astCompilationUnits, RuleContext ctx, Language language) {
    visitor.visitAll(astCompilationUnits, ctx);
  }

}
