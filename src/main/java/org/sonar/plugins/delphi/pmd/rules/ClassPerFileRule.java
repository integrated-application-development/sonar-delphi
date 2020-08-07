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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.StructTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class ClassPerFileRule extends AbstractDelphiRule {
  private int count;

  @Override
  public void start(RuleContext ctx) {
    count = 0;
  }

  @Override
  public void end(RuleContext ctx) {
    int limit = getProperty(LIMIT);
    if (count > limit) {
      newViolation(ctx)
          .atPosition(FilePosition.atFileLevel())
          .message(
              String.format("File has %d classes, maximum number of classes is %d.", count, limit))
          .save();
    }
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (shouldCount(type)) {
      ++count;
    }
    return super.visit(type, data);
  }

  private static boolean shouldCount(TypeDeclarationNode type) {
    return type.isClass()
        && !type.isNestedType()
        && !type.isForwardDeclaration()
        && !((StructTypeNode) type.getTypeNode()).getVisibilitySections().isEmpty();
  }
}
