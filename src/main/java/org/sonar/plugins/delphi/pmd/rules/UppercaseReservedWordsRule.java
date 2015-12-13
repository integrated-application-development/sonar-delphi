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

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import net.sourceforge.pmd.RuleContext;

/**
 * Rule checking if we are using some keyword with all uppercase characters.
 */
public class UppercaseReservedWordsRule extends DelphiRule {

  private static final String[] KEYWORDS_A = {"ASM"};
  private static final String[] KEYWORDS_B = {"BEGIN"};
  private static final String[] KEYWORDS_C = {"CASE", "CLASS", "CONST", "CONSTRUCTOR"};
  private static final String[] KEYWORDS_D = {"DESTRUCTOR", "DISPINTERFACE", "DIV", "DO", "DOWNTO"};
  private static final String[] KEYWORDS_E = {"ELSE", "END", "EXCEPT", "EXPORTS"};
  private static final String[] KEYWORDS_F = {"FILE", "FINAL", "FINALIZATION", "FINALLY", "FOR", "FUNCTION"};
  private static final String[] KEYWORDS_G = {"GOTO"};
  private static final String[] KEYWORDS_I = {"IF", "IMPLEMENTATION", "IN", "INHERITED", "INITIALIZATION", "INLINE",
    "INTERFACE", "IS"};
  private static final String[] KEYWORDS_L = {"LABEL", "LIBRARY"};
  private static final String[] KEYWORDS_N = {"NIL", "NOT"};
  private static final String[] KEYWORDS_O = {"OF", "OR", "OUT"};
  private static final String[] KEYWORDS_P = {"PACKED", "PROCEDURE", "PROGRAM", "PROPERTY"};
  private static final String[] KEYWORDS_R = {"RAISE", "RECORD", "REMOVE", "REPEAT", "RESOURCESTRING"};
  private static final String[] KEYWORDS_S = {"SEALED", "SET", "SHL", "SHR", "STATIC", "STRICT", "STRING"};
  private static final String[] KEYWORDS_T = {"THEN", "THREADVAR", "TO", "TRY", "TYPE"};
  private static final String[] KEYWORDS_U = {"UNIT", "UNSAFE", "UNTIL", "USES"};
  private static final String[] KEYWORDS_W = {"WHILE", "WITH"};
  private static final String[] KEYWORDS_V = {"VAR"};

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (StringUtils.isEmpty(node.getText())) {
      return;
    }
    char firstChar = node.getText().charAt(0);
    switch (firstChar) {
      case 'A':
        checkKeyword(node.getText(), KEYWORDS_A, node, ctx);
        break;
      case 'B':
        checkKeyword(node.getText(), KEYWORDS_B, node, ctx);
        break;
      case 'C':
        checkKeyword(node.getText(), KEYWORDS_C, node, ctx);
        break;
      case 'D':
        checkKeyword(node.getText(), KEYWORDS_D, node, ctx);
        break;
      case 'E':
        checkKeyword(node.getText(), KEYWORDS_E, node, ctx);
        break;
      case 'F':
        checkKeyword(node.getText(), KEYWORDS_F, node, ctx);
        break;
      case 'G':
        checkKeyword(node.getText(), KEYWORDS_G, node, ctx);
        break;
      case 'I':
        checkKeyword(node.getText(), KEYWORDS_I, node, ctx);
        break;
      case 'L':
        checkKeyword(node.getText(), KEYWORDS_L, node, ctx);
        break;
      case 'N':
        checkKeyword(node.getText(), KEYWORDS_N, node, ctx);
        break;
      case 'O':
        checkKeyword(node.getText(), KEYWORDS_O, node, ctx);
        break;
      case 'P':
        checkKeyword(node.getText(), KEYWORDS_P, node, ctx);
        break;
      case 'R':
        checkKeyword(node.getText(), KEYWORDS_R, node, ctx);
        break;
      case 'S':
        checkKeyword(node.getText(), KEYWORDS_S, node, ctx);
        break;
      case 'T':
        checkKeyword(node.getText(), KEYWORDS_T, node, ctx);
        break;
      case 'U':
        checkKeyword(node.getText(), KEYWORDS_U, node, ctx);
        break;
      case 'W':
        checkKeyword(node.getText(), KEYWORDS_W, node, ctx);
        break;
      case 'V':
        checkKeyword(node.getText(), KEYWORDS_V, node, ctx);
        break;
    }
  }

  protected void checkKeyword(String keyword, String[] keywords, DelphiPMDNode node, RuleContext ctx) {
    for (String key : keywords) {
      if (keyword.equals(key)) {
        String msg = "Avoid using uppercase keywords: " + keyword;
        addViolation(ctx, node, msg);
      }
    }
  }

}
