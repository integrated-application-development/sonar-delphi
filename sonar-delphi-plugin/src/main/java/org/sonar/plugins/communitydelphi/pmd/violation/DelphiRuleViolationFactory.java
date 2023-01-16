/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.pmd.violation;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRuleViolationFactory;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.pmd.FilePosition;
import org.sonar.plugins.communitydelphi.pmd.rules.DelphiRule;

public class DelphiRuleViolationFactory extends AbstractRuleViolationFactory {
  @Override
  protected RuleViolation createRuleViolation(
      Rule rule, RuleContext ruleContext, Node node, String message) {
    return ((DelphiRule) rule)
        .newViolation(ruleContext)
        .atPosition(FilePosition.from((DelphiNode) node))
        .atLocation((DelphiNode) node)
        .message(message)
        .build();
  }

  @Override
  protected RuleViolation createRuleViolation(
      Rule rule, RuleContext ruleContext, Node node, String message, int beginLine, int endLine) {
    return ((DelphiRule) rule)
        .newViolation(ruleContext)
        .atPosition(FilePosition.atLineLevel(beginLine, endLine))
        .atLocation((DelphiNode) node)
        .message(message)
        .build();
  }
}
