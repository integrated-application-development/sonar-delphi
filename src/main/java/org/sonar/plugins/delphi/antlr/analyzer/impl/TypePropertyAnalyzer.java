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
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClassProperty;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;

/**
 * Analyzes property fields
 */
public class TypePropertyAnalyzer extends CodeAnalyzer {

    @Override
    protected void doAnalyze(CodeTree codeTree, CodeAnalysisResults results) {
        if (results.getActiveClass() == null) {
            throw new IllegalStateException("Cannot parse class fields for no active class");
        }

        String varType = getPropertyType((CommonTree) codeTree.getCurrentCodeNode().getNode());
        if (varType == null) {
            return;
        }

        String varName = getPropertyName((CommonTree) codeTree.getCurrentCodeNode().getNode());
        FunctionInterface read = getPropertyReadFunction((CommonTree) codeTree.getCurrentCodeNode().getNode());
        FunctionInterface write = getPropertyWriteFunction((CommonTree) codeTree.getCurrentCodeNode().getNode());

        if (read != null) {
            read.setParentClass(results.getActiveClass());
        }

        if (write != null) {
            write.setParentClass(results.getActiveClass());
        }

        ClassPropertyInterface property = new DelphiClassProperty(varName, varType, results.getParseVisibility()
                .toMetrics(), read, write);
        property.setParent(results.getActiveClass());
        results.getActiveClass().addProperty(property);
    }

    @Override
    public boolean canAnalyze(CodeTree codeTree) {
        return codeTree.getCurrentCodeNode().getNode().getType() == LexerMetrics.PROPERTY.toMetrics();
    }

    private FunctionInterface getPropertyReadFunction(CommonTree node) {
        Tree functionNode = node.getFirstChildWithType(LexerMetrics.READ.toMetrics());
        if (functionNode != null) {
            String functionName = functionNode.getChild(0).getText();
            FunctionInterface function = new DelphiFunction();
            function.setName(functionName);
            return function;
        }
        return null;
    }

    private FunctionInterface getPropertyWriteFunction(CommonTree node) {
        Tree functionNode = node.getFirstChildWithType(LexerMetrics.WRITE.toMetrics());
        if (functionNode != null) {
            String functionName = functionNode.getChild(0).getText();
            FunctionInterface function = new DelphiFunction();
            function.setName(functionName);
            return function;
        }
        return null;
    }

    private String getPropertyName(CommonTree node) {
        Tree nameNode = node.getFirstChildWithType(LexerMetrics.VARIABLE_IDENTS.toMetrics());
        return nameNode.getChild(0).getText();
    }

    private String getPropertyType(CommonTree node) {
        Tree typeNode = node.getFirstChildWithType(LexerMetrics.VARIABLE_TYPE.toMetrics()).getChild(0);
        if (typeNode != null) {
            return typeNode.getText();
        }
        return null;
    }

}
