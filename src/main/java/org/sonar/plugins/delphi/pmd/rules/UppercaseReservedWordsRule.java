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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule checking if we are using some keyword with all uppercase characters. Raises a violation if
 * they are uppercase
 */
public class UppercaseReservedWordsRule extends DelphiRule {


  // The set of keywords to avoid using complete capitalisation with. Individually listed to avoid
  // highlighting all
  private static final Set<Integer> keywords = new HashSet<>(Arrays.asList(
      DelphiLexer.ASM, DelphiLexer.BEGIN, DelphiLexer.CASE, DelphiLexer.CLASS,
      DelphiLexer.CONST, DelphiLexer.CONSTRUCTOR, DelphiLexer.DESTRUCTOR,
      DelphiLexer.DISPINTERFACE, DelphiLexer.DIV, DelphiLexer.DO, DelphiLexer.DOWNTO,
      DelphiLexer.ELSE, DelphiLexer.EXCEPT, DelphiLexer.EXPORTS, DelphiLexer.FILE,
      DelphiLexer.FINAL, DelphiLexer.FINALIZATION, DelphiLexer.FINALLY, DelphiLexer.FOR,
      DelphiLexer.FUNCTION, DelphiLexer.GOTO, DelphiLexer.IF, DelphiLexer.IMPLEMENTATION,
      DelphiLexer.IN, DelphiLexer.INHERITED, DelphiLexer.INITIALIZATION, DelphiLexer.INLINE,
      DelphiLexer.INTERFACE, DelphiLexer.IS, DelphiLexer.LABEL, DelphiLexer.LIBRARY,
      DelphiLexer.NIL, DelphiLexer.NOT, DelphiLexer.OF, DelphiLexer.OR, DelphiLexer.OUT,
      DelphiLexer.PACKED, DelphiLexer.PROCEDURE, DelphiLexer.PROGRAM, DelphiLexer.PROPERTY,
      DelphiLexer.PROGRAM, DelphiLexer.PROPERTY, DelphiLexer.RAISE, DelphiLexer.RECORD,
      DelphiLexer.REMOVE, DelphiLexer.REPEAT, DelphiLexer.RESOURCESTRING, DelphiLexer.SEALED,
      DelphiLexer.SET, DelphiLexer.SHL, DelphiLexer.SHR, DelphiLexer.STATIC, DelphiLexer.STRICT,
      DelphiLexer.THEN, DelphiLexer.THREADVAR, DelphiLexer.TO, DelphiLexer.TRY,
      DelphiLexer.TYPE, DelphiLexer.UNIT, DelphiLexer.UNSAFE, DelphiLexer.UNTIL,
      DelphiLexer.USES, DelphiLexer.WHILE, DelphiLexer.WITH, DelphiLexer.VAR));

  /**
   * If any of the above rules are written in complete uppercase, a violation will be raised
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    int nodeType = node.getType();
    if (keywords.contains(nodeType)) {
      String keywordName = node.getText();
      if (checkKeyword(keywordName)) {
        addViolation(ctx, node);
      }

    }
  }

  private boolean checkKeyword(String keywordName) {
    // Check not all are uppercase
    String uppercaseConventionRegex = "[A-Z]+";
    return keywordName.matches(uppercaseConventionRegex);
  }

}
