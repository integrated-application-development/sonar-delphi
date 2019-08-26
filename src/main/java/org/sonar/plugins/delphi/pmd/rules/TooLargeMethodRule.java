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

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import java.util.function.Predicate;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

/** Class for counting method statements. If too many, creates a violation. */
public class TooLargeMethodRule extends AbstractDelphiRule {
  private static final String VIOLATION_MESSAGE =
      "%s is too large. Method has %d statements (Limit is %d)";

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    long statements = countStatements(method);
    int limit = getProperty(LIMIT);

    if (statements > limit) {
      addViolationWithMessage(
          data,
          method.getMethodHeading().getMethodName(),
          String.format(VIOLATION_MESSAGE, method.getSimpleName(), statements, limit));
    }

    return super.visit(method, data);
  }

  private long countStatements(MethodImplementationNode method) {
    if (method.hasMethodBody()) {
      MethodBodyNode body = method.getMethodBody();
      if (body.hasStatementBlock()) {
        int handlers = body.getStatementBlock().findDescendantsOfType(ExceptItemNode.class).size();
        long statements =
            body.getStatementBlock()
                .statementStream()
                .filter(Predicate.not(CompoundStatementNode.class::isInstance))
                .count();

        return handlers + statements;
      }
    }
    return 0;
  }
}
