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
package org.sonar.plugins.delphi.antlr.ast;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTreeAdaptor;

/**
 * DelphiLanguage Tree Adaptor class, used for creating DelphiPMDNodes in ANTLR
 */
class DelphiTreeAdaptor extends CommonTreeAdaptor {

  private ASTTree astTree;
  private int lastLine = 0;

  /**
   * Adaptor ctor
   * 
   * @param tree Tree
   */
  public DelphiTreeAdaptor(ASTTree tree) {
    astTree = tree;
  }

  @Override
  public Object create(Token payload) {
    if (payload != null) {
      lastLine = payload.getLine();
    }
    return new DelphiPMDNode(payload, astTree);
  }

  /**
   * When creating imaginary Tokens (such as TkReturnType etc) we need to set
   * its parameters manually.
   */

  @Override
  public Token createToken(int tokenType, String text) {
    CommonToken imaginaryToken = new CommonToken(tokenType, text);
    imaginaryToken.setLine(lastLine);
    return imaginaryToken;
  }
}
