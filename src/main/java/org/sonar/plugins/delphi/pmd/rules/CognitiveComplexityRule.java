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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.CognitiveComplexityVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.CognitiveComplexityVisitor.Data;

public class CognitiveComplexityRule extends AbstractDelphiRule {
  private static final CognitiveComplexityVisitor COGNITIVE_VISITOR =
      new CognitiveComplexityVisitor() {
        @Override
        public Data visit(MethodBodyNode body, Data data) {
          // Skip the block declaration section so we don't count sub-procedures.
          return body.getBlock().accept(this, data);
        }
      };

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    Data result = COGNITIVE_VISITOR.visit(method, new Data());
    int complexity = result.getComplexity();
    int limit = getProperty(LIMIT);

    if (complexity > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "The Cognitive Complexity of this method \"{0}\""
              + " is {1} which is greater than {2} authorized.",
          new Object[] {method.simpleName(), complexity, limit});
    }
    return super.visit(method, data);
  }
}
