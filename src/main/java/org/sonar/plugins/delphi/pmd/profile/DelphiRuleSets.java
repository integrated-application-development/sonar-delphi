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
package org.sonar.plugins.delphi.pmd.profile;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import org.sonar.plugins.delphi.pmd.DelphiRuleChain;
import net.sourceforge.pmd.lang.ast.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class representing Delphi rule set
 */
public class DelphiRuleSets extends RuleSets {

  private DelphiRuleChain delphiRuleChain = new DelphiRuleChain();
  private Collection<RuleSet> ruleSets = new ArrayList<>();

  @Override
  public void apply(List<Node> acuList, RuleContext ctx, Language language) {
    for (RuleSet ruleSet : ruleSets) {
      if (ruleSet.applies(ctx.getSourceCodeFile())) {
        ruleSet.apply(acuList, ctx);
      }
    }
  }

  @Override
  public void addRuleSet(RuleSet ruleSet) {
    ruleSets.add(ruleSet);
    delphiRuleChain.add(ruleSet);
  }

  @Override
  public boolean applies(File file) {
    return true;
  }

}
