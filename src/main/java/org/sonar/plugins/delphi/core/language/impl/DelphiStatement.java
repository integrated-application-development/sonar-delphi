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
package org.sonar.plugins.delphi.core.language.impl;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.Tokenizer;

/**
 * DelphiLanguage class statement definition
 *
 * @see StatementInterface
 */
public class DelphiStatement implements StatementInterface {

  private int line = -1;
  private int column = -1;
  private String text;
  private boolean complex;
  private Tokenizer tokenizer = new Tokenizer();

  /**
   * Ctor
   *
   * @param text Statement text
   * @param lineNumber Statement line number
   * @param columnNumber Statement column number
   */
  public DelphiStatement(String text, int lineNumber, int columnNumber) {
    line = lineNumber;
    column = columnNumber;
    setText(text);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final int getLine() {
    return line;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final int getColumn() {
    return column;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final String getText() {
    return text;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final void setText(String value) {
    text = value;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final void setLine(int value) {
    line = value;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final void setColumn(int value) {
    column = value;
  }

  @Override
  public ClassFieldInterface[] getFields(ClassInterface fromClass) {
    if (fromClass == null) {
      return new ClassFieldInterface[0];
    }
    ClassFieldInterface[] fields = fromClass.getFields();
    List<ClassFieldInterface> result = new ArrayList<>();

    List<Token> tokens = tokenizer.tokenize(new String[]{text});

    for (Token token : tokens) {
      if (token.getType() != DelphiLexer.TkIdentifier) {
        continue;
      }
      for (ClassFieldInterface field : fields) {
        if (field.getName().equals(token.getText())) {
          result.add(field);
        }
      }
    }
    return result.toArray(new ClassFieldInterface[0]);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final void setComplexity(boolean isComplex) {
    complex = isComplex;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public final boolean isComplex() {
    return complex;
  }

  @Override
  public String toString() {
    return text + " (line: " + line + " column: " + column + " complex: " + complex + ")";
  }

}
