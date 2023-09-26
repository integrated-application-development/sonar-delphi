/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;

public final class VariableUtils {
  private VariableUtils() {
    // Utility class
  }

  public static boolean isGeneratedFormVariable(VarDeclarationNode varDecl) {
    VarSectionNode varSection = varDecl.getVarSection();
    if (varSection.getFirstParentOfType(InterfaceSectionNode.class) == null) {
      return false;
    }

    if (!(varSection.getScope() instanceof FileScope)) {
      return false;
    }

    if (varSection.getDeclarations().size() != 1) {
      return false;
    }

    if (varSection.getChildIndex() != varSection.getParent().getChildren().size() - 1) {
      return false;
    }

    return varDecl.getType().isSubTypeOf("System.Classes.TComponent");
  }
}
