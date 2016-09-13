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

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Cast And Free rule - don't cast, just to free something, example:
 * TMyObject(sth).free; (sth as TMyObject).free;
 */
public class CastAndFreeRule extends DelphiRule {

  private int sequenceHardCastIndex = 0;
  private int sequenceSoftCastIndex = 0;
  private LexerMetrics hardCastSequence[] = {LexerMetrics.IDENT, LexerMetrics.LPAREN, LexerMetrics.IDENT,
    LexerMetrics.RPAREN,
    LexerMetrics.DOT, LexerMetrics.IDENT};
  private LexerMetrics softCastSequence[] = {LexerMetrics.LPAREN, LexerMetrics.IDENT, LexerMetrics.AS,
    LexerMetrics.IDENT,
    LexerMetrics.RPAREN, LexerMetrics.DOT, LexerMetrics.IDENT};

  @Override
  public void init() {
    sequenceHardCastIndex = 0;
    sequenceSoftCastIndex = 0;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    sequenceHardCastIndex = processSequence(hardCastSequence, sequenceHardCastIndex, node, ctx);
    sequenceSoftCastIndex = processSequence(softCastSequence, sequenceSoftCastIndex, node, ctx);
  }

  private int processSequence(LexerMetrics sequence[], int sequenceIndex, DelphiPMDNode node, RuleContext ctx) {
    int resultIndex = sequenceIndex;
    if (resultIndex >= sequence.length) {
      resultIndex = 0;
    }
    else if (sequence[resultIndex].toMetrics() == node.getType()) {
      ++resultIndex;
      if (isCorrectSequence(sequence, resultIndex, node)) {
        resultIndex = 0;
        addViolation(ctx, node);
      }
    }
    else {
      resultIndex = 0;
    }

    return resultIndex;
  }

  private boolean isCorrectSequence(LexerMetrics sequence[], int index, Tree lastNode) {
    return index >= sequence.length && "free".equalsIgnoreCase(lastNode.getText());
  }
}
