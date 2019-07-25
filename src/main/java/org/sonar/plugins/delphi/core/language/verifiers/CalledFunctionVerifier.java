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
package org.sonar.plugins.delphi.core.language.verifiers;

import com.qualinsight.plugins.sonarqube.smell.api.annotation.Smell;
import com.qualinsight.plugins.sonarqube.smell.api.model.SmellType;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;

/** Verifies if we are calling a function, from a given tree node */
public class CalledFunctionVerifier {

  private boolean isUnresolved = true;
  private CodeAnalysisResults results;
  private FunctionInterface calledFunction;

  /**
   * ctor
   *
   * @param results to hold results of source code parsing
   */
  public CalledFunctionVerifier(CodeAnalysisResults results) {
    if (results == null) {
      throw new IllegalArgumentException(
          "CalledFunctionVerifier constructor param 'results' cannot be null.");
    }
    this.results = results;
  }

  @Smell(
      minutes = 30,
      reason =
          "We can't assume that a function call always looks like an identifier followed by '(' "
              + "or ';', considering empty parentheses are optional in Delphi."
              + "Example: 'Result := FunctionWithoutArguments + 5;' will not see a function call."
              + "It's indistinguishable from a variable until we have a symbol table."
              + "This causes false positives in dead code analysis.",
      type = SmellType.WRONG_LOGIC)
  public boolean verify(Tree node) {
    // if we are on a ident token and it is not last
    if (looksLikeFunctionCall(node)) {
      String functionName = node.getText().toLowerCase();
      List<UnitInterface> unitsToLook = new ArrayList<>();
      // first we look in current unit for function reference
      unitsToLook.add(results.getActiveUnit());
      unitsToLook.addAll(results.getActiveUnit().getIncludedUnits(results.getCachedUnits()));

      for (UnitInterface unit : unitsToLook) {
        if (resolveFunction(functionName, unit)) {
          return true;
        }
      }

      // create a new unresolved function
      calledFunction = new DelphiFunction(node.getText().toLowerCase());
      isUnresolved = true;
      return true;
    }

    // not a function call (not like "foo(args);" or "foo;"
    return false;
  }

  private boolean looksLikeFunctionCall(Tree node) {
    CommonTree nextNode = (CommonTree) node.getParent().getChild(node.getChildIndex() + 1);

    return node.getType() == LexerMetrics.IDENT.toMetrics()
        && nextNode != null
        && (nextNode.getType() == LexerMetrics.LPAREN.toMetrics()
            || nextNode.getType() == LexerMetrics.SEMI.toMetrics());
  }

  private boolean resolveFunction(String functionName, UnitInterface unit) {
    FunctionInterface[] functions = unit.getAllFunctions();
    for (FunctionInterface func : functions) {
      if (func.getShortName().equalsIgnoreCase(functionName)) {
        calledFunction = func;
        isUnresolved = false;
        return true;
      }
    }
    return false;
  }

  public FunctionInterface fetchCalledFunction() {
    return calledFunction;
  }

  public boolean isUnresolvedFunctionCall() {
    return isUnresolved;
  }
}
