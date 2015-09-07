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

/**
 * Base class doing AST node analisys, extend it to provide custom analyzers
 */
public abstract class CodeAnalyzer {

  private CodeAnalyzer successor;

  /**
   * add new CodeAnalyzer to chain
   * 
   * @param successor code analyzer we put into chain
   * @return successor
   */
  public CodeAnalyzer chain(CodeAnalyzer successor) {
    this.successor = successor;
    return successor;
  }

  /**
   * analyes code tree with a set of chaied analyzers
   * 
   * @param codeTree code tree to analyze
   * @param results code analysis results holder
   */
  public void analyze(CodeTree codeTree, CodeAnalysisResults results) {
    if (canAnalyze(codeTree)) {
      doAnalyze(codeTree, results);
    }
    if (successor != null) {
      successor.analyze(codeTree, results);
    }
  }

  protected CodeAnalyzer getSuccesor() {
    return successor;
  }

  protected boolean hasSucessor() {
    return successor != null;
  }

  protected abstract void doAnalyze(CodeTree codeTree, CodeAnalysisResults results);

  /**
   * can a current code analyzer analyze this code tree?
   * 
   * @param codeTree code tree to check
   * @return true if can analyze, false otherwise
   */
  public abstract boolean canAnalyze(CodeTree codeTree);
}
