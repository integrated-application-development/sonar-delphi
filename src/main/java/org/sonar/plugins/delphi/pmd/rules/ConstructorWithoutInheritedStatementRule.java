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

import java.util.ArrayDeque;
import java.util.Deque;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;

public class ConstructorWithoutInheritedStatementRule extends NoInheritedStatementRule {

  private final Deque<String> recordTypes = new ArrayDeque<>();

  @Override
  public void start(RuleContext ctx) {
    recordTypes.clear();
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isRecord()) {
      recordTypes.add(type.getQualifiedName());
    }
    return super.visit(type, data);
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (shouldCheck(method)) {
      checkViolation(method, data);
    }
    return super.visit(method, data);
  }

  private boolean shouldCheck(MethodImplementationNode method) {
    return method.isConstructor()
        && !method.isClassMethod()
        && !recordTypes.contains(method.getTypeName());
  }
}
