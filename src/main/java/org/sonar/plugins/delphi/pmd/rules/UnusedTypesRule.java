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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

public class UnusedTypesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    NameDeclarationNode name = type.getTypeNameNode();
    if (canBeUnused(type.getType())
        && name.getUsages().stream()
            .allMatch(occurrence -> isWithinType(occurrence, type.getType()))) {
      addViolation(data, name);
    }
    return super.visit(type, data);
  }

  private static boolean canBeUnused(Type type) {
    return !(type instanceof HelperType);
  }

  private static boolean isWithinType(NameOccurrence occurrence, Type type) {
    Scope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      Scope typeScope = DelphiScope.unknownScope();
      if (scope instanceof MethodScope) {
        typeScope = ((MethodScope) scope).getTypeScope();
      } else if (scope instanceof TypeScope) {
        typeScope = scope;
      }

      Type foundType = DelphiType.unknownType();
      if (typeScope instanceof TypeScope) {
        foundType = ((TypeScope) typeScope).getType();
      }

      if (type.is(foundType)) {
        return true;
      }

      scope = scope.getParent();
    }
    return false;
  }
}
