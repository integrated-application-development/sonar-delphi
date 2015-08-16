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
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClassField;

/**
 * Analyzes class fields (TkClassField token)
 */
public class TypeFieldsAnalyzer extends CodeAnalyzer {

    @Override
    protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
        if (results.getActiveClass() == null) {
            throw new IllegalStateException("Cannot parse class fields for no active class");
        }

        String varTypeName = getClassVarTypeName((CommonTree) codeTree.getCurrentCodeNode().getNode());
        String varNames = getClassVarName((CommonTree) codeTree.getCurrentCodeNode().getNode());
        String[] names = varNames.split(",");
        for (String name : names) {
            ClassFieldInterface field = new DelphiClassField(name, varTypeName, results.getParseVisibility()
                    .toMetrics());
            field.setParent(results.getActiveClass());
            results.getActiveClass().addField(field);
        }
    }

    @Override
    public boolean canAnalyze(CodeTree codeTree) {
        return codeTree.getCurrentCodeNode().getNode().getType() == LexerMetrics.CLASS_FIELD.toMetrics();
    }

    private String getClassVarName(CommonTree variableNode) {
        StringBuilder name = new StringBuilder();
        CommonTree nameNode = (CommonTree) variableNode.getFirstChildWithType(LexerMetrics.VARIABLE_IDENTS.toMetrics());
        if (nameNode != null) {
            Tree node = nameNode;
            while ((node = node.getChild(0)) != null) {
                name.append(node.getText());
                if (node.getChildCount() != 0) {
                    name.append(',');
                }
            }
        }
        return name.toString();
    }

    private String getClassVarTypeName(CommonTree node) {
        StringBuilder name = new StringBuilder();
        Tree typeNode = node.getFirstChildWithType(LexerMetrics.VARIABLE_TYPE.toMetrics());
        for (int i = 0; i < typeNode.getChildCount(); ++i) {
            name.append(typeNode.getChild(i).getText());
        }
        return name.toString();
    }

}
