/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.UnresolvedFunctionCall;

/**
 * Class used for function analysis
 */
public class FunctionAnalyzer extends CodeAnalyzer {

  private static final LexerMetrics FUNCTION_NODE_TYPE[] = { LexerMetrics.FUNCTION, LexerMetrics.PROCEDURE, LexerMetrics.DESTRUCTOR,
      LexerMetrics.CONSTRUCTOR };

  private String functionName;
  private String functionRealName;
  private List<String> functionProperties;

  private int functionLine;
  private int functionCharPosition;

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    int type = codeTree.getCurrentCodeNode().getNode().getType();
    for (LexerMetrics metric : FUNCTION_NODE_TYPE) {
      if (type == metric.toMetrics()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    ClassInterface currentClass = results.getActiveClass(); // null?
    if (results.getActiveUnit() == null) {
      throw new IllegalStateException("Cannot create function outside unit.");
    }

    functionRealName = getFunctionName((CommonTree) codeTree.getCurrentCodeNode().getNode());
    if (functionRealName.isEmpty()) { // no name => no function
      return;
    }
    functionName = functionRealName.toLowerCase(); // gets function name
    functionName = checkFunctionName(functionName, currentClass, results);

    functionProperties = getFunctionProperties(codeTree.getCurrentCodeNode().getNode());
    FunctionInterface activeFunction = createFunction(results, currentClass);
    processFunction(activeFunction, results, currentClass);
    results.setActiveFunction(activeFunction);
  }

  private List<String> getFunctionProperties(Tree node) {
    List<String> props = new ArrayList<String>();
    for (int i = 0; i < node.getChildCount(); ++i) {
      Tree child = node.getChild(i);
      if (child.getText().equalsIgnoreCase("override")) {
        props.add("virtual");
      } else if (child.getText().equalsIgnoreCase("virtual")) {
        props.add("virtual");
      }
    }
    return props;
  }

  private String checkFunctionName(String functionName, ClassInterface currentClass, CodeAnalysisResults results) {
    if (currentClass != null) {
      return currentClass.getName() + '.' + functionName; // new name with class prefix
    } else {
      if (functionName.lastIndexOf('.') > -1) {
        return functionName; // no new name
      }

      for (ClassInterface c : results.getClasses()) // check all classes
      {
        if (functionName.startsWith(c.getName() + ".")) {
          return c.getName() + '.' + functionName; // new name with class prefix
        }
      }
    }

    return functionName; // no new name
  }

  private String getFunctionName(CommonTree functionNode) {
    Tree nameNode = functionNode.getFirstChildWithType(LexerMetrics.FUNCTION_NAME.toMetrics());
    if (nameNode == null) {
      return "";
    }

    functionLine = nameNode.getLine();
    functionCharPosition = nameNode.getCharPositionInLine();

    StringBuilder str = new StringBuilder();
    for (int i = 0; i < nameNode.getChildCount(); ++i) {
      Tree child = nameNode.getChild(i);
      str.append(child.getText());
    }
    return str.toString();
  }

  private FunctionInterface createFunction(CodeAnalysisResults results, ClassInterface currentClass) {
    FunctionInterface activeFunction = results.getCachedFunction(functionName); // was function already created?
    if (activeFunction == null) // NOT created, make a new one
    {
      activeFunction = new DelphiFunction(); // create a new function
      activeFunction.setName(functionName.toLowerCase()); // function name
      activeFunction.setRealName(functionRealName); // real name with no lowercase
      activeFunction.setLine(functionLine); // fn line in file
      activeFunction.setVisibility(results.getParseVisibility().toMetrics()); // sets the function visibility (private, public, protected)
      activeFunction.setColumn(functionCharPosition); // fn column in file
      activeFunction.setUnit(results.getActiveUnit()); // function unit
      activeFunction.setParentClass(currentClass); // function parent class

      if (functionProperties.contains("virtual")) {
        activeFunction.setVirtual(true);
      }

      results.addFunction(activeFunction);
      results.getActiveUnit().addFunction(activeFunction); // add function to unit
      results.cacheFunction(functionName, activeFunction); // put to set of cached functions (ALL functions)

      // check for unresolved function calls
      UnresolvedFunctionCall unresolved = results.getUnresolvedCalls().get(activeFunction.getShortName());
      if (unresolved != null && unresolved.resolve(activeFunction, results.getCachedUnits())) {
        results.getUnresolvedCalls().remove(activeFunction.getShortName());
      }
    }
    return activeFunction;
  }

  private void processFunction(FunctionInterface activeFunction, CodeAnalysisResults results, ClassInterface currentClass) {
    if (activeFunction.isGlobal() && !results.hasFunction(activeFunction)) { // if we found an global function before,
      results.addFunction(activeFunction); // add it to this file
    }

    if (results.getParseVisibility() == LexerMetrics.PUBLIC) {
      activeFunction.setVisibility(results.getParseVisibility().toMetrics()); // sets the function visibility (private, public, protected)
    }

    if (results.getParseStatus() == LexerMetrics.INTERFACE) // if in interface section
    {
      activeFunction.setDeclaration(true); // function is only a declaration
      if (currentClass != null) {
        currentClass.addFunction(activeFunction); // when adding declaration from interface
      }
    }
  }
}