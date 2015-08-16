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
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.core.language.UnitInterface;

/**
 * Analyzes includes
 */
public class IncludeAnalyzer extends CodeAnalyzer {

    private int includeIndex;

    @Override
    public void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
        analyzeIncludes(codeTree.getCurrentCodeNode().getNode(), results.getActiveUnit());
    }

    @Override
    public boolean canAnalyze(CodeTree codeTree) {
        int type = codeTree.getCurrentCodeNode().getNode().getType();
        return (type == DelphiParser.USES || type == DelphiParser.UNIT || type == DelphiParser.LIBRARY);
    }

    private void analyzeIncludes(Tree includeNode, UnitInterface activeUnit) {
        if (activeUnit == null || includeNode == null) {
            return;
        }
        includeIndex = 0;
        CommonTree node;
        while ((node = getNextUnitIncludeNode(includeNode)) != null) {
            activeUnit.addIncludes(node.getText());
        }
    }

    protected CommonTree getNextUnitIncludeNode(Tree node) {
        CommonTree result = (CommonTree) node.getChild(includeIndex++);
        if (result == null) {
            includeIndex = 0;
        }
        return result;
    }

}
