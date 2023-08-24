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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.IndentationUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.LibraryDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.PackageDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ProgramDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UsesClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "UnitLevelKeywordIndentationRule", repositoryKey = "delph")
@Rule(key = "UnitLevelKeywordIndentation")
public class UnitLevelKeywordIndentationCheck extends DelphiCheck {
  private static final String MESSAGE = "Unindent this unit-level keyword.";

  private static void checkNodeIndentation(DelphiNode node, DelphiCheckContext context) {
    if (!IndentationUtils.getLineIndentation(node).equals("")) {
      context
          .newIssue()
          .onFilePosition(FilePosition.from(node.getToken()))
          .withMessage(MESSAGE)
          .report();
    }
  }

  private static DelphiNode getEnd(DelphiNode node) {
    return node.getFirstChildWithTokenType(DelphiTokenType.END);
  }

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    DelphiNode end = getEnd(ast);
    if (end != null) {
      checkNodeIndentation(getEnd(ast), context);
    }
    return super.visit(ast, context);
  }

  @Override
  public DelphiCheckContext visit(
      UnitDeclarationNode unitDeclarationNode, DelphiCheckContext context) {
    checkNodeIndentation(unitDeclarationNode, context);
    return super.visit(unitDeclarationNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      ProgramDeclarationNode programDeclarationNode, DelphiCheckContext context) {
    checkNodeIndentation(programDeclarationNode, context);
    return super.visit(programDeclarationNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      LibraryDeclarationNode libraryDeclarationNode, DelphiCheckContext context) {
    checkNodeIndentation(libraryDeclarationNode, context);
    return super.visit(libraryDeclarationNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      PackageDeclarationNode packageDeclarationNode, DelphiCheckContext context) {
    checkNodeIndentation(packageDeclarationNode, context);
    return super.visit(packageDeclarationNode, context);
  }

  @Override
  public DelphiCheckContext visit(UsesClauseNode usesClauseNode, DelphiCheckContext context) {
    checkNodeIndentation(usesClauseNode, context);
    return super.visit(usesClauseNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      InterfaceSectionNode interfaceSectionNode, DelphiCheckContext context) {
    checkNodeIndentation(interfaceSectionNode, context);
    return super.visit(interfaceSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      ImplementationSectionNode implementationSectionNode, DelphiCheckContext context) {
    checkNodeIndentation(implementationSectionNode, context);
    return super.visit(implementationSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(TypeSectionNode typeSectionNode, DelphiCheckContext context) {
    if (typeSectionNode.getParent() instanceof InterfaceSectionNode
        || typeSectionNode.getParent() instanceof ImplementationSectionNode) {
      checkNodeIndentation(typeSectionNode, context);
    }
    return super.visit(typeSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(VarSectionNode varSectionNode, DelphiCheckContext context) {
    if (varSectionNode.getParent() instanceof InterfaceSectionNode
        || varSectionNode.getParent() instanceof ImplementationSectionNode) {
      checkNodeIndentation(varSectionNode, context);
    }
    return super.visit(varSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(ConstSectionNode constSectionNode, DelphiCheckContext context) {
    if (constSectionNode.getParent() instanceof InterfaceSectionNode
        || constSectionNode.getParent() instanceof ImplementationSectionNode) {
      checkNodeIndentation(constSectionNode, context);
    }
    return super.visit(constSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      CompoundStatementNode compoundStatementNode, DelphiCheckContext context) {
    if (compoundStatementNode.getParent() instanceof DelphiAst) {
      checkNodeIndentation(compoundStatementNode, context);
      checkNodeIndentation(getEnd(compoundStatementNode), context);
    }
    return super.visit(compoundStatementNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      InitializationSectionNode initializationSectionNode, DelphiCheckContext context) {
    checkNodeIndentation(initializationSectionNode, context);
    return super.visit(initializationSectionNode, context);
  }

  @Override
  public DelphiCheckContext visit(
      FinalizationSectionNode finalizationSectionNode, DelphiCheckContext context) {
    checkNodeIndentation(finalizationSectionNode, context);
    return super.visit(finalizationSectionNode, context);
  }
}
