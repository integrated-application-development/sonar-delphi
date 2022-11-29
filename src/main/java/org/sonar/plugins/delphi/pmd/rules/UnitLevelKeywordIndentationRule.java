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
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.LibraryDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PackageDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProgramDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UsesClauseNode;
import org.sonar.plugins.delphi.utils.IndentationUtils;

public class UnitLevelKeywordIndentationRule extends AbstractDelphiRule {
  private void checkNodeIndentation(DelphiNode node, RuleContext data) {
    if (!IndentationUtils.getLineIndentation(node).equals("")) {
      addViolation(data, node);
    }
  }

  @Override
  public RuleContext visit(InitializationSectionNode initializationSectionNode, RuleContext data) {
    checkNodeIndentation(initializationSectionNode, data);
    return super.visit(initializationSectionNode, data);
  }

  @Override
  public RuleContext visit(InterfaceSectionNode interfaceSectionNode, RuleContext data) {
    checkNodeIndentation(interfaceSectionNode, data);
    return super.visit(interfaceSectionNode, data);
  }

  @Override
  public RuleContext visit(ImplementationSectionNode implementationSectionNode, RuleContext data) {
    checkNodeIndentation(implementationSectionNode, data);
    return super.visit(implementationSectionNode, data);
  }

  @Override
  public RuleContext visit(FinalizationSectionNode finalizationSectionNode, RuleContext data) {
    checkNodeIndentation(finalizationSectionNode, data);
    return super.visit(finalizationSectionNode, data);
  }

  @Override
  public RuleContext visit(UnitDeclarationNode unitDeclarationNode, RuleContext data) {
    checkNodeIndentation(unitDeclarationNode, data);
    return super.visit(unitDeclarationNode, data);
  }

  @Override
  public RuleContext visit(ProgramDeclarationNode programDeclarationNode, RuleContext data) {
    checkNodeIndentation(programDeclarationNode, data);
    return super.visit(programDeclarationNode, data);
  }

  @Override
  public RuleContext visit(LibraryDeclarationNode libraryDeclarationNode, RuleContext data) {
    checkNodeIndentation(libraryDeclarationNode, data);
    return super.visit(libraryDeclarationNode, data);
  }

  @Override
  public RuleContext visit(PackageDeclarationNode packageDeclarationNode, RuleContext data) {
    checkNodeIndentation(packageDeclarationNode, data);
    return super.visit(packageDeclarationNode, data);
  }

  @Override
  public RuleContext visit(UsesClauseNode usesClauseNode, RuleContext data) {
    checkNodeIndentation(usesClauseNode, data);
    return super.visit(usesClauseNode, data);
  }

  @Override
  public RuleContext visit(CompoundStatementNode compoundStatementNode, RuleContext data) {
    // ONLY top level compound statements
    if (compoundStatementNode.jjtGetParent() instanceof DelphiAST) {
      checkNodeIndentation(compoundStatementNode, data);
    }
    return super.visit(compoundStatementNode, data);
  }

  @Override
  public RuleContext visit(TypeSectionNode typeSectionNode, RuleContext data) {
    // ONLY top level type declarations
    if (typeSectionNode.jjtGetParent() instanceof InterfaceSectionNode
        || typeSectionNode.jjtGetParent() instanceof ImplementationSectionNode) {
      checkNodeIndentation(typeSectionNode, data);
    }
    return super.visit(typeSectionNode, data);
  }
}
