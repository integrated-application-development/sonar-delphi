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
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;

/** Delphi class analyzer, used to analyze types in a source file */
public class TypeAnalyzer extends CodeAnalyzer {

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    CommonTree currentNode = (CommonTree) codeTree.getCurrentCodeNode().getNode();
    if (currentNode.getType() != DelphiParser.TkNewType) {
      return false;
    }

    int type = getType(currentNode);

    return type == DelphiLexer.TkClass
        || type == DelphiLexer.TkRecord
        || type == DelphiLexer.TkInterface;
  }

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    if (results.getActiveUnit() == null) {
      throw new IllegalStateException(
          "AbstractAnalyser::parseClass() - Cannot create class outside unit.");
    }

    Tree nameNode = getClassNameNode((CommonTree) codeTree.getCurrentCodeNode().getNode());
    if (nameNode == null) {
      throw new IllegalStateException("AbstractAnalyser::parseClass() - Cannot get class name.");
    }

    final String fileName = codeTree.getRootCodeNode().getNode().getFileName().toLowerCase();

    ClassInterface searchClass = new DelphiClass(nameNode.getText().toLowerCase());
    searchClass.setFileName(fileName);

    ClassInterface active = results.getCachedClass(searchClass);
    if (active == null) {
      active = new DelphiClass(nameNode.getText().toLowerCase());
      active.setFileName(fileName);
      results.cacheClass(active);
      results.getActiveUnit().addClass(active);
    }

    if (results.getParseStatus() == LexerMetrics.IMPLEMENTATION) {
      active.setVisibility(DelphiLexer.PRIVATE);
    } else {
      active.setVisibility(DelphiLexer.PUBLIC);
    }

    results.setParseVisibility(LexerMetrics.PUBLISHED);
    results.getClasses().add(active);
    results.setActiveClass(active);
  }

  private int getType(CommonTree node) {
    Tree typeNode = node.getFirstChildWithType(DelphiLexer.TkNewTypeDecl).getChild(0);
    return typeNode.getType();
  }

  private Tree getClassNameNode(CommonTree node) {
    return node.getFirstChildWithType(DelphiLexer.TkNewTypeName).getChild(0);
  }
}
