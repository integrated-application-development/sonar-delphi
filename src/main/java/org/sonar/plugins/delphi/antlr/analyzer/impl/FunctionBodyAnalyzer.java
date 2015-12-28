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

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.UnresolvedFunctionCall;
import org.sonar.plugins.delphi.core.language.verifiers.CalledFunctionVerifier;
import org.sonar.plugins.delphi.core.language.verifiers.StatementVerifier;

/**
 * Analyzes body of a function
 */
public class FunctionBodyAnalyzer extends CodeAnalyzer {

  private CodeAnalysisResults results = null;
  private StatementVerifier statementverifier;

  private static final LexerMetrics[] BRANCHING_NODES = {LexerMetrics.IF, LexerMetrics.FOR, LexerMetrics.WHILE,
    LexerMetrics.CASE,
    LexerMetrics.REPEAT, LexerMetrics.AND, LexerMetrics.OR};

  /**
   * ctor
   * 
   * @param results
   * @param delphiProjectHelper
   */
  public FunctionBodyAnalyzer(CodeAnalysisResults results, DelphiProjectHelper delphiProjectHelper) {
    if (results == null) {
      throw new IllegalArgumentException("FunctionBodyAnalyzer ctor 'results' parameter cannot be null.");
    }
    this.results = results;
    this.statementverifier = new StatementVerifier(delphiProjectHelper);
  }

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    boolean hasActiveFunction = results.getActiveFunction() != null;
    boolean isFunctionBodyNode = isBodyNode(codeTree.getCurrentCodeNode().getNode().getType());
    return hasActiveFunction && isFunctionBodyNode;
  }

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    FunctionInterface activeFunction = results.getActiveFunction();
    FunctionInterface functionHolder = activeFunction;

    Tree beginNode = codeTree.getCurrentCodeNode().getNode();

    // increases function overload, so the starting overload should be -1
    activeFunction.increaseFunctionOverload();
    activeFunction.setBodyLine(extractLine(codeTree.getCurrentCodeNode().getNode()));

    if (activeFunction.getOverloadsCount() > 0) {
      functionHolder = new DelphiFunction();
      functionHolder.setName(activeFunction.getName());
      functionHolder.setLongName(activeFunction.getLongName());
      functionHolder.setLine(activeFunction.getLine());
      functionHolder.setBodyLine(activeFunction.getBodyLine());
      activeFunction.addOverloadFunction(functionHolder);
    }

    countStatements(beginNode, functionHolder);
    countCalledFunctions(beginNode, functionHolder, results);

    if (!functionHolder.isAccessor()) {
      functionHolder.increaseComplexity();
      countBranches(beginNode, functionHolder);
    }

    results.setActiveFunction(null);
  }

  private int extractLine(Tree currentCodeNode) {
    Tree parent = currentCodeNode.getParent();
    for (int i = currentCodeNode.getChildIndex() - 1; i >= 0; i--) {
      Tree child = parent.getChild(i);
      if (child.getType() == DelphiLexer.FUNCTION
        || child.getType() == DelphiLexer.PROCEDURE
        || child.getType() == DelphiLexer.CONSTRUCTOR
        || child.getType() == DelphiLexer.DESTRUCTOR
        || child.getType() == DelphiLexer.OPERATOR) {
        return child.getLine();
      }
    }
    return -1;
  }

  /**
   * Only functions existing in your project and in include directories are
   * counted, so system functions like 'writeln' are NOT counted.
   */
  private void countCalledFunctions(Tree node, FunctionInterface function, CodeAnalysisResults results) {
    CalledFunctionVerifier verifyer = new CalledFunctionVerifier(results);
    if (verifyer.verify(node)) {
      FunctionInterface calledFunction = verifyer.fetchCalledFunction();
      if (verifyer.isUnresolvedFunctionCall()) {
        UnresolvedFunctionCall unresolvedCall = new UnresolvedFunctionCall(function, calledFunction,
          results.getActiveUnit());
        results.addUnresolvedCall(calledFunction.getName(), unresolvedCall);
      } else {
        function.addCalledFunction(calledFunction);
      }
    }

    // do the same for all children
    for (int i = 0; i < node.getChildCount(); ++i) {
      countCalledFunctions(node.getChild(i), function, results);
    }

  }

  private void countStatements(Tree node, FunctionInterface function) {
    if (statementverifier.verify(node)) {
      StatementInterface st = statementverifier.createStatement();
      function.addStatement(st);
    }

    for (int i = 0; i < node.getChildCount(); ++i) {
      countStatements(node.getChild(i), function);
    }
  }

  private boolean isBodyNode(int type) {
    return type == LexerMetrics.FUNCTION_BODY.toMetrics();
  }

  private void countBranches(Tree node, FunctionInterface function) {
    if (node == null || function == null) {
      return;
    }

    if (isBranchingNode(node)) {
      function.increaseComplexity();
    }

    for (int i = 0; i < node.getChildCount(); ++i) {
      countBranches(node.getChild(i), function);
    }
  }

  private boolean isBranchingNode(Tree node) {
    for (LexerMetrics metric : BRANCHING_NODES) {
      if (metric.toMetrics() == node.getType()) {
        return true;
      }
    }
    return false;
  }

}
