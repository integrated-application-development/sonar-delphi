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
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.core.language.impl.UnresolvedFunctionCall;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used for function analysis
 */
public class FunctionAnalyzer extends CodeAnalyzer {

  private static final String PROP_MESSAGE = "message";
  private static final String PROP_VIRTUAL = "virtual";

  private static final LexerMetrics FUNCTION_NODE_TYPE[] = {LexerMetrics.FUNCTION,
    LexerMetrics.PROCEDURE,
    LexerMetrics.DESTRUCTOR,
    LexerMetrics.CONSTRUCTOR,
    LexerMetrics.OPERATOR};

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
      UnitInterface defaultUnit = new DelphiUnit("Default");
      if (!results.getCachedUnits().contains(defaultUnit)) {
        results.cacheUnit(defaultUnit);
      }
      results.setActiveUnit(defaultUnit);
    }

    functionRealName = getFunctionName((CommonTree) codeTree.getCurrentCodeNode().getNode());
    if (StringUtils.isEmpty(functionRealName)) {
      return;
    }

    functionName = checkFunctionName(functionRealName.toLowerCase(), currentClass, results).toLowerCase();

    functionProperties = getFunctionProperties(codeTree.getCurrentCodeNode().getNode());
    FunctionInterface activeFunction = createFunction(results, currentClass);
    processFunction(activeFunction, results, currentClass);
    results.setActiveFunction(activeFunction);
  }

  private List<String> getFunctionProperties(Tree node) {
    List<String> props = new ArrayList<String>();
    for (int i = 0; i < node.getChildCount(); ++i) {
      Tree child = node.getChild(i);
      if ("override".equalsIgnoreCase(child.getText())) {
        props.add(PROP_VIRTUAL);
      } else if (PROP_VIRTUAL.equalsIgnoreCase(child.getText())) {
        props.add(PROP_VIRTUAL);
      } else if (PROP_MESSAGE.equalsIgnoreCase(child.getText())) {
        props.add(PROP_MESSAGE);
      }
    }
    return props;
  }

  private String checkFunctionName(String functionName, ClassInterface currentClass, CodeAnalysisResults results) {
    if (currentClass != null) {
      if (functionName.startsWith(currentClass.getName().toLowerCase())) {
        return functionName;
      }
      // new name with class prefix
      return currentClass.getName() + '.' + functionName;
    } else {
      if (functionName.lastIndexOf('.') > -1) {
        return functionName;
      }

      // check all classes
      for (ClassInterface c : results.getClasses())
      {
        if (functionName.startsWith(c.getName() + ".")) {
          // new name with class prefix
          return c.getName() + '.' + functionName;
        }
      }
    }

    return functionName;
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
    FunctionInterface activeFunction = results.getCachedFunction(functionName);
    if (activeFunction == null)
    {
      activeFunction = new DelphiFunction();
      activeFunction.setName(functionName.toLowerCase());
      activeFunction.setRealName(functionRealName);
      activeFunction.setLine(functionLine);
      activeFunction.setVisibility(results.getParseVisibility().toMetrics());
      activeFunction.setColumn(functionCharPosition);
      activeFunction.setUnit(results.getActiveUnit());
      activeFunction.setParentClass(currentClass);

      if (functionProperties.contains(PROP_VIRTUAL)) {
        activeFunction.setVirtual(true);
      }

      if (functionProperties.contains(PROP_MESSAGE)) {
        activeFunction.setMessage(true);
      }

      results.addFunction(activeFunction);
      results.getActiveUnit().addFunction(activeFunction);
      results.cacheFunction(functionName, activeFunction);

      // check for unresolved function calls
      UnresolvedFunctionCall unresolved = results.getUnresolvedCalls().get(activeFunction.getShortName());
      if (unresolved != null && unresolved.resolve(activeFunction, results.getCachedUnits())) {
        results.getUnresolvedCalls().remove(activeFunction.getShortName());
      }
    }
    return activeFunction;
  }

  private void processFunction(FunctionInterface activeFunction, CodeAnalysisResults results,
    ClassInterface currentClass) {
    // if we found a global function before, add it to this file
    if (activeFunction.isGlobal() && !results.hasFunction(activeFunction)) {
      results.addFunction(activeFunction);
    }

    if (results.getParseVisibility() == LexerMetrics.PUBLIC) {
      activeFunction.setVisibility(results.getParseVisibility().toMetrics());
    }

    if (results.getParseStatus() == LexerMetrics.INTERFACE) {
      // function is only a declaration
      activeFunction.setDeclaration(true);
      if (currentClass != null) {
        currentClass.addFunction(activeFunction);
      }
    }
  }
}
