/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.utils;

import au.com.integradev.delphi.antlr.ast.node.VarDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.VarSectionNode;
import au.com.integradev.delphi.symbol.scope.FileScope;

public class VariableUtils {
  private VariableUtils() {
    // Utility class
  }

  public static boolean isGeneratedFormVariable(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    if (!varSection.isInterfaceSection()) {
      return false;
    }

    if (!(varSection.getScope() instanceof FileScope)) {
      return false;
    }

    if (varSection.getDeclarations().size() != 1) {
      return false;
    }

    if (varSection.jjtGetChildIndex() != varSection.jjtGetParent().jjtGetNumChildren() - 1) {
      return false;
    }

    return varDecl.getType().isSubTypeOf("System.Classes.TComponent");
  }
}
