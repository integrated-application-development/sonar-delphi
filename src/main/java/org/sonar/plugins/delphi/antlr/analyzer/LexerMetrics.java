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
package org.sonar.plugins.delphi.antlr.analyzer;

import org.sonar.plugins.delphi.antlr.DelphiLexer;

/**
 * Enum class holing lexer metrics used during file analysis
 */
public enum LexerMetrics {
  PRIVATE(0, DelphiLexer.PRIVATE),
  PUBLIC(1, DelphiLexer.PUBLIC),
  PROTECTED(2, DelphiLexer.PROTECTED),
  PUBLISHED(3, DelphiLexer.PUBLISHED),
  IMPLEMENTATION(4, DelphiLexer.IMPLEMENTATION),
  INTERFACE(5, DelphiLexer.INTERFACE),
  FILE(6, DelphiLexer.FILE),
  FUNCTION_BODY(7, DelphiLexer.BEGIN),
  IDENT(8, DelphiLexer.TkIdentifier),
  UNIT(9, DelphiLexer.UNIT),
  LIBRARY(10, DelphiLexer.LIBRARY),
  CLASS_FIELD(11, DelphiLexer.TkClassField),
  VARIABLE_TYPE(12, DelphiLexer.TkVariableType),
  VARIABLE_IDENTS(13, DelphiLexer.TkVariableIdents),
  PROPERTY(14, DelphiLexer.PROPERTY),
  FUNCTION(15, DelphiLexer.FUNCTION),
  PROCEDURE(16, DelphiLexer.PROCEDURE),
  DESTRUCTOR(17, DelphiLexer.DESTRUCTOR),
  CONSTRUCTOR(18, DelphiLexer.CONSTRUCTOR),
  READ(19, DelphiLexer.READ),
  WRITE(20, DelphiLexer.WRITE),
  CLASS_PARENTS(21, DelphiLexer.TkClassParents),
  FUNCTION_NAME(22, DelphiLexer.TkFunctionName),
  VAR(23, DelphiLexer.VAR),
  FUNCTION_ARGS(24, DelphiLexer.TkFunctionArgs),
  FOR(25, DelphiLexer.FOR),
  IF(26, DelphiLexer.IF),
  WHILE(27, DelphiLexer.WHILE),
  REPEAT(28, DelphiLexer.REPEAT),
  AND(29, DelphiLexer.AND),
  OR(30, DelphiLexer.OR),
  CASE(31, DelphiLexer.CASE),
  BREAK(32, DelphiLexer.BREAK),
  CONTINUE(33, DelphiLexer.CONTINUE),
  ELSE(34, DelphiLexer.ELSE),
  BEGIN(35, DelphiLexer.BEGIN),
  END(36, DelphiLexer.END),
  TRY(37, DelphiLexer.TRY),
  ASSIGN(38, DelphiLexer.ASSIGN),
  SEMI(39, DelphiLexer.SEMI),
  LPAREN(40, DelphiLexer.LPAREN),
  RPAREN(41, DelphiLexer.RPAREN),
  USES(42, DelphiLexer.USES),
  NEW_TYPE(43, DelphiLexer.TkNewType),
  EXCEPT(44, DelphiLexer.EXCEPT),
  WITH(45, DelphiLexer.WITH),
  THEN(46, DelphiLexer.THEN),
  DO(47, DelphiLexer.DO),
  DOT(48, DelphiLexer.DOT),
  AS(49, DelphiLexer.AS),
  OPERATOR(50, DelphiLexer.OPERATOR);

  private final int code;
  private final int metrics;

  LexerMetrics(int code, int metrics) {
    this.code = code;
    this.metrics = metrics;
  }

  /**
   * @return Delphi lexer metrics
   */
  public int toMetrics() {
    return metrics;
  }

  /**
   * @return metric id
   */
  public int toCode() {
    return code;
  }

  /**
   * @param code delphi lexer id
   * @return lexer metrics
   */
  public static LexerMetrics getLexerMetricsForType(int code) {
    LexerMetrics[] values = LexerMetrics.values();
    for (int i = 0; i < values.length; i++) {
      LexerMetrics value = values[i];
      if (value.metrics == code) {
        return value;
      }
    }
    throw new IllegalStateException("No LexerMetrics for metric: " + code);
  }
}
