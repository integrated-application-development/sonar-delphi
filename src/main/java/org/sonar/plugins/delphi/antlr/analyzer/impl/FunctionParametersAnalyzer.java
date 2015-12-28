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

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.ArgumentInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiArgument;

/**
 * Analyzes function parameters (arguments)
 */
public class FunctionParametersAnalyzer extends CodeAnalyzer {

  public static final String UNTYPED_PARAMETER_NAME = "UntypedParameter";

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    if (results.getActiveFunction() == null) {
      throw new IllegalArgumentException("FunctionParametersAnalyzer activeFunction cannot be null.");
    }

    StringBuilder argumentTypes = new StringBuilder("(");

    FunctionInterface activeFunction = results.getActiveFunction();
    List<ArgumentInterface> arguments = getFunctionArguments(codeTree);
    for (ArgumentInterface argument : arguments) {
      activeFunction.addArgument(argument);
      argumentTypes.append(argument.getType() + "; ");
    }

    argumentTypes.append(")");
    activeFunction.setLongName(activeFunction.getName() + argumentTypes.toString());
  }

  private List<ArgumentInterface> getFunctionArguments(CodeTree codeTree) {
    List<ArgumentInterface> result = new ArrayList<ArgumentInterface>();
    Tree parentNode = codeTree.getCurrentCodeNode().getNode();

    for (int i = 0; i < parentNode.getChildCount(); ++i) {
      Tree childNode = parentNode.getChild(i);
      int type = childNode.getType();
      if (type != LexerMetrics.VARIABLE_IDENTS.toMetrics()) {
        continue;
      }

      List<String> argumentNames = getArgumentNames(childNode);
      String argumentType = getArgumentTypes(childNode);
      for (String name : argumentNames) {
        result.add(new DelphiArgument(name, argumentType));
      }
    }

    return result;
  }

  private String getArgumentTypes(Tree nameNode) {
    Tree typeNode = nameNode.getParent().getChild(nameNode.getChildIndex() + 1);
    if (typeNode.getChildCount() > 0) {
      return typeNode.getChild(0).getText();
    }
    return UNTYPED_PARAMETER_NAME;
  }

  private List<String> getArgumentNames(Tree nameNode) {
    List<String> names = new ArrayList<String>();
    for (int i = 0; i < nameNode.getChildCount(); ++i) {
      names.add(nameNode.getChild(i).getText());
    }
    return names;
  }

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    return codeTree.getCurrentCodeNode().getNode().getType() == LexerMetrics.FUNCTION_ARGS.toMetrics();
  }

}
