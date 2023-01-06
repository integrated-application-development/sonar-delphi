/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.PointerTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeReferenceNode;

public class PointerNameRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (isViolation(type)) {
      addViolation(data, type.getTypeNameNode());
    }

    return super.visit(type, data);
  }

  private static boolean isViolation(TypeDeclarationNode type) {
    if (type.isPointer()) {
      TypeNode typeNode = ((PointerTypeNode) type.getTypeNode()).getDereferencedTypeNode();
      if (typeNode instanceof TypeReferenceNode) {
        TypeReferenceNode referenceNode = (TypeReferenceNode) typeNode;
        String dereferencedName = referenceNode.simpleName();
        String expected = expectedPointerName(dereferencedName);
        return !type.simpleName().equals(expected);
      }
    }
    return false;
  }

  private static String expectedPointerName(String dereferencedName) {
    return "P" + dereferencedName.substring(dereferencedName.startsWith("T") ? 1 : 0);
  }
}
