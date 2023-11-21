/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.LocalDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

@Rule(key = "ConsecutiveVarSection")
public class ConsecutiveVarSectionCheck extends AbstractConsecutiveSectionCheck {
  @Override
  protected String getSectionName() {
    return "var";
  }

  @Override
  protected boolean areViolatingSections(DelphiNode first, DelphiNode second) {
    if (first instanceof VarSectionNode && second instanceof VarSectionNode) {
      VarSectionNode firstSection = (VarSectionNode) first;
      VarSectionNode secondSection = (VarSectionNode) second;
      return firstSection.isThreadVarSection() == secondSection.isThreadVarSection();
    } else {
      return false;
    }
  }

  @Override
  public DelphiCheckContext visit(InterfaceSectionNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(ImplementationSectionNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(LocalDeclarationSectionNode node, DelphiCheckContext context) {
    checkViolation(node, context);
    return super.visit(node, context);
  }
}
