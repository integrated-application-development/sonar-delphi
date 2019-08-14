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
package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.ParseException;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;

/** Preforms PMD check for Delphi source files */
public class DelphiPMD {

  private final Report report = new Report();

  /**
   * Processes the file read by the reader against the rule set.
   *
   * @param pmdFile input source file
   * @param ruleSets set of rules to process against the file
   * @param ctx context in which PMD is operating. This contains the Renderer and whatnot
   * @param fileStreamConfig configures the filestream that creates the AST
   */
  public void processFile(
      File pmdFile, RuleSets ruleSets, RuleContext ctx, DelphiFileStreamConfig fileStreamConfig) {
    ctx.setSourceCodeFile(pmdFile);
    ctx.setReport(report);

    if (ruleSets.applies(ctx.getSourceCodeFile())) {
      Language language = LanguageRegistry.getLanguage(DelphiLanguageModule.LANGUAGE_NAME);
      ctx.setLanguageVersion(language.getDefaultVersion());

      DelphiAST ast = new DelphiAST(pmdFile, fileStreamConfig);

      if (ast.isError()) {
        throw new ParseException("grammar error");
      }

      ruleSets.start(ctx);
      ruleSets.apply(getNodesFromAST(ast), ctx, language);
      ruleSets.end(ctx);
    }
  }

  /**
   * @param ast AST tree
   * @return AST tree nodes ready for parsing by PMD
   */
  public List<Node> getNodesFromAST(DelphiAST ast) {
    List<Node> nodes = new ArrayList<>();
    indexNodes(ast, nodes);

    return nodes;
  }

  /**
   * Adds children nodes to list
   *
   * @param parent The node whose children are being indexed
   * @param list List of DelphiPMDNodes being built
   */
  private void indexNodes(Tree parent, List<Node> list) {
    for (int i = 0; i < parent.getChildCount(); ++i) {
      Tree child = parent.getChild(i);
      list.add((DelphiNode) child);
      indexNodes(parent.getChild(i), list);
    }
  }

  /**
   * Gets generated report
   *
   * @return Report
   */
  public Report getReport() {
    return report;
  }
}
