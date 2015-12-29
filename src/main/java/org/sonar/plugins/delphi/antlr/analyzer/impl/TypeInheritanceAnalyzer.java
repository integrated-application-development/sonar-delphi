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
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;

/**
 * Analyzes inheritance tree for specific class
 * 
 */
public class TypeInheritanceAnalyzer extends CodeAnalyzer {

  @Override
  protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
    if (results.getActiveClass() == null) {
      throw new IllegalStateException("Analyzing class parents for no active class");
    }

    for (int i = 0; i < codeTree.getCurrentCodeNode().getNode().getChildCount(); ++i) {
      CommonTree parentNode = (CommonTree) codeTree.getCurrentCodeNode().getNode().getChild(i);
      String parentName = parentNode.getText().toLowerCase();
      ClassInterface parentClass = checkParentInFile(parentName, results);
      if (parentClass == null) {
        parentClass = checkParentInUnits(parentName, results);
      }

      ClassInterface searchClass = new DelphiClass(parentName);
      for (String uses : results.getActiveUnit().getIncludes()) {
        searchClass.setFileName(uses);
        parentClass = results.getCachedClass(searchClass);
        if (parentClass != null) {
          break;
        }
      }

      if (parentClass == null) {
        parentClass = new DelphiClass(parentName);

        // Unit not found on uses. Assuming it's the same file.
        parentClass.setFileName(results.getActiveClass().getFileName());

        results.cacheClass(parentClass);
      }

      results.getActiveClass().addParent(parentClass);
    }
  }

  @Override
  public boolean canAnalyze(CodeTree codeTree) {
    return codeTree.getCurrentCodeNode().getNode().getType() == LexerMetrics.CLASS_PARENTS.toMetrics();
  }

  /**
   * check if parent is in one of the parsed units
   */
  private ClassInterface checkParentInUnits(String parentName, CodeAnalysisResults results) {
    for (UnitInterface unit : results.getCachedUnits()) {
      // if not in this unit, continue
      if (!results.getActiveUnit().isIncluding(unit)) {
        continue;
      }
      ClassInterface found = unit.findClass(parentName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * check if parent is in one of classes in current file
   */
  private ClassInterface checkParentInFile(String parentName, CodeAnalysisResults results) {
    for (ClassInterface clazz : results.getClasses()) {
      if (!clazz.equals(results.getActiveClass()) && clazz.getShortName().equalsIgnoreCase(parentName)) {
        return clazz;
      }
    }
    return null;
  }

}
