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
package org.sonar.plugins.delphi.antlr.analyzer.impl.operations;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

class OperationsTestsCommon // private class
{

    protected final static int LAYER_NODES = 3;

    protected NodeOperation operation;
    protected Tree parent;

    public OperationsTestsCommon(NodeOperation op) {
        operation = op;
    }

    public void init() {
        parent = new CommonTree(new CommonToken(256, "parent"));
        for (int i = 0; i < LAYER_NODES; ++i) {
            CommonTree child = new CommonTree(new CommonToken(i + 1, "layer1"));
            child.addChild(new CommonTree(new CommonToken(i + 100, "layer2")));
            parent.addChild(child);
        }
    }

}
