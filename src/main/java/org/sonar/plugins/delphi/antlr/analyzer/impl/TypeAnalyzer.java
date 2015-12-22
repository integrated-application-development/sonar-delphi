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
package org.sonar.plugins.delphi.antlr.analyzer.impl;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;

/**
 * Delphi class analyzer, used to analyze types in a source file
 */
public class TypeAnalyzer extends CodeAnalyzer {

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    Tree currentNode = codeTree.getCurrentCodeNode().getNode();
    if (currentNode.getType() != DelphiParser.TkNewType || (!hasGrandChild(currentNode))) {
      return false;
    }

    int type = getGrandChild(currentNode).getType();
    return type == DelphiLexer.TkClass || type == DelphiLexer.TkRecord || type == DelphiLexer.TkInterface;
  }

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    if (results.getActiveUnit() == null) {
      throw new IllegalStateException("AbstractAnalyser::parseClass() - Cannot create class outside unit.");
    }

    CommonTree nameNode = getClassNameNode(codeTree.getCurrentCodeNode().getNode());
    if (nameNode == null) {
      throw new IllegalStateException("AbstractAnalyser::parseClass() - Cannot get class name.");
    }

    ClassInterface active = results.getCachedClass(nameNode.getText().toLowerCase());
    if (active == null) {
      active = new DelphiClass(nameNode.getText().toLowerCase());
      results.cacheClass(active.toString(), active);
      results.getActiveUnit().addClass(active);
    }

    active.setFileName(codeTree.getRootCodeNode().getNode().getFileName().toLowerCase());

    if (results.getParseStatus() == LexerMetrics.IMPLEMENTATION) {
      active.setVisibility(LexerMetrics.PRIVATE.toMetrics());
    } else {
      active.setVisibility(LexerMetrics.PUBLIC.toMetrics());
    }

    results.setParseVisibility(LexerMetrics.PUBLISHED);
    results.getClasses().add(active);
    results.setActiveClass(active);
  }

  private boolean hasGrandChild(Tree node) {
    return getGrandChild(node) != null;
  }

  private Tree getGrandChild(Tree node) {
    Tree child = node.getChild(0);
    if (child != null) {
      return child.getChild(0);
    }
    return null;
  }

  private CommonTree getClassNameNode(Tree node) {
    return (CommonTree) node.getChild(0);
  }

}
