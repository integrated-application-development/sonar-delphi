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

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.impl.FunctionAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.FunctionBodyAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.FunctionParametersAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.IncludeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.InterfaceAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.TypeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.TypeFieldsAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.TypeInheritanceAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.TypePropertyAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.UnitAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.VisibilityAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;

/**
 * Class for analyzing a DelphiLanguage AST tree
 */
public class DelphiASTAnalyzer implements ASTAnalyzer {

  private CodeAnalysisResults result;
  private CodeTree code;
  private DelphiProjectHelper delphiProjectHelper;

  public DelphiASTAnalyzer(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void analyze(ASTTree tree) {
    result = new CodeAnalysisResults();
    code = new CodeTree(new CodeNode<ASTTree>(tree), new CodeNode<Tree>(tree.getChild(0)));

    CodeAnalyzer analyzer = new UnitAnalyzer();
    analyzer.chain(new IncludeAnalyzer()).chain(new InterfaceAnalyzer()).chain(new VisibilityAnalyzer())
      .chain(new TypeAnalyzer())
      .chain(new TypeInheritanceAnalyzer()).chain(new TypeFieldsAnalyzer()).chain(new TypePropertyAnalyzer())
      .chain(new FunctionAnalyzer()).chain(new FunctionBodyAnalyzer(result, delphiProjectHelper))
      .chain(new FunctionParametersAnalyzer());

    CodeNode<Tree> codeNode = code.getCurrentCodeNode();
    AdvanceNodeOperation advance = new AdvanceNodeOperation();
    while (codeNode.isValid()) {
      analyzer.analyze(code, result);
      codeNode = advance.execute(codeNode.getNode());
      code.setCurrentNode(codeNode);
    }
  }

  @Override
  public CodeAnalysisResults getResults() {
    return result;
  }

  @Override
  public CodeTree getCode() {
    return code;
  }

  @Override
  public boolean hasResults() {
    return result != null;
  }

}
