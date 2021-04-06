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
package org.sonar.plugins.delphi.antlr.ast.visitors;

import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AncestorListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.AnsiStringTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayConstructorNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayIndicesNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.BlockDeclarationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseItemStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassReferenceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstArraySubTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ContainsClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.CustomAttributeListNode;
import org.sonar.plugins.delphi.antlr.ast.node.CustomAttributeNode;
import org.sonar.plugins.delphi.antlr.ast.node.DecimalLiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ElseBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinallyBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForInStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForToStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterListNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericArgumentsNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode;
import org.sonar.plugins.delphi.antlr.ast.node.GotoStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.HelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.HexLiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.IfStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImportClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.IntegerLiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceGuidNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.LabelStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.LibraryDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodResolutionClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodReturnTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationListNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.NilLiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.ObjectTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.PackageDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ParenthesizedExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PointerTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProceduralTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureReferenceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProgramDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyReadSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyWriteSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.QualifiedNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.RangeExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordExpressionItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordVariantItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordVariantSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordVariantTagNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.RequiresClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.SetTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.SimpleNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.StringTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.StructTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.SubRangeTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TextLiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.TryStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeAliasNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeOfTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.node.UsesClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.VisibilityNode;
import org.sonar.plugins.delphi.antlr.ast.node.VisibilitySectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.WhileStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.WithStatementNode;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;

public interface DelphiParserVisitor<T> {

  default T visit(DelphiNode node, T data) {
    return node.childrenAccept(this, data);
  }

  default T visit(CommonDelphiNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(DelphiAST node, T data) {
    for (DelphiToken token : node.getTokens()) {
      visitToken(token, data);
    }

    return visit((DelphiNode) node, data);
  }

  default void visitToken(DelphiToken token, T data) {
    // Do nothing
  }

  default T visit(ArgumentListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ArrayAccessorNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ArrayIndicesNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(BlockDeclarationSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AncestorListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ConstArraySubTypeNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ConstDeclarationNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ConstSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(CustomAttributeNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(CustomAttributeListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ElseBlockNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(EnumElementNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ExceptBlockNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ExceptItemNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FieldDeclarationNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FieldSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FinalizationSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FinallyBlockNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FormalParameterListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(FormalParameterNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ForLoopVarNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ForLoopVarDeclarationNode node, T data) {
    return visit((ForLoopVarNode) node, data);
  }

  default T visit(ForLoopVarReferenceNode node, T data) {
    return visit((ForLoopVarNode) node, data);
  }

  default T visit(GenericArgumentsNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(GenericDefinitionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(NameDeclarationListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(IdentifierNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ImplementationSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(InitializationSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(InterfaceGuidNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(InterfaceSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodBodyNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodHeadingNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodNameNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodParametersNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(NameReferenceNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodResolutionClauseNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodReturnTypeNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ProcedureTypeHeadingNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(PropertyNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(PropertyReadSpecifierNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(PropertyWriteSpecifierNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(RecordExpressionItemNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(RecordVariantItemNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(RecordVariantSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(RecordVariantTagNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(StatementListNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(TypeDeclarationNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(TypeParameterNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(TypeSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(UnitImportNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(VarDeclarationNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(VarSectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(VisibilityNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(VisibilitySectionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  /* Import clauses */
  default T visit(ImportClauseNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(ContainsClauseNode node, T data) {
    return visit((ImportClauseNode) node, data);
  }

  default T visit(RequiresClauseNode node, T data) {
    return visit((ImportClauseNode) node, data);
  }

  default T visit(UsesClauseNode node, T data) {
    return visit((ImportClauseNode) node, data);
  }

  /* Name declarations */
  default T visit(NameDeclarationNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(SimpleNameDeclarationNode node, T data) {
    return visit((NameDeclarationNode) node, data);
  }

  default T visit(QualifiedNameDeclarationNode node, T data) {
    return visit((NameDeclarationNode) node, data);
  }

  /* Methods */
  default T visit(MethodNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(MethodDeclarationNode node, T data) {
    return visit((MethodNode) node, data);
  }

  default T visit(MethodImplementationNode node, T data) {
    return visit((MethodNode) node, data);
  }

  /* File headers */
  default T visit(FileHeaderNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(LibraryDeclarationNode node, T data) {
    return visit((FileHeaderNode) node, data);
  }

  default T visit(PackageDeclarationNode node, T data) {
    return visit((FileHeaderNode) node, data);
  }

  default T visit(ProgramDeclarationNode node, T data) {
    return visit((FileHeaderNode) node, data);
  }

  default T visit(UnitDeclarationNode node, T data) {
    return visit((FileHeaderNode) node, data);
  }

  /* Types */
  default T visit(TypeNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AnsiStringTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(ArrayTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(ClassReferenceTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(EnumTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(FileTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(PointerTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(SetTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(StringTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(SubRangeTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(TypeAliasNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(TypeOfTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(TypeReferenceNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(TypeTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  /* Procedural types */
  default T visit(ProceduralTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(MethodTypeNode node, T data) {
    return visit((ProceduralTypeNode) node, data);
  }

  default T visit(ProcedureReferenceTypeNode node, T data) {
    return visit((ProceduralTypeNode) node, data);
  }

  default T visit(ProcedureTypeNode node, T data) {
    return visit((ProceduralTypeNode) node, data);
  }

  /* Struct types */
  default T visit(StructTypeNode node, T data) {
    return visit((TypeNode) node, data);
  }

  default T visit(ClassTypeNode node, T data) {
    return visit((StructTypeNode) node, data);
  }

  default T visit(InterfaceTypeNode node, T data) {
    return visit((StructTypeNode) node, data);
  }

  default T visit(ObjectTypeNode node, T data) {
    return visit((StructTypeNode) node, data);
  }

  default T visit(RecordTypeNode node, T data) {
    return visit((StructTypeNode) node, data);
  }

  /* Helper types */
  default T visit(HelperTypeNode node, T data) {
    return visit((StructTypeNode) node, data);
  }

  default T visit(ClassHelperTypeNode node, T data) {
    return visit((HelperTypeNode) node, data);
  }

  default T visit(RecordHelperTypeNode node, T data) {
    return visit((HelperTypeNode) node, data);
  }

  /* Expressions */
  default T visit(ExpressionNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AnonymousMethodNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(ArrayConstructorNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(ArrayExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(BinaryExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(ParenthesizedExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(PrimaryExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(RecordExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(UnaryExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  default T visit(RangeExpressionNode node, T data) {
    return visit((ExpressionNode) node, data);
  }

  /* Literals */
  default T visit(LiteralNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(DecimalLiteralNode node, T data) {
    return visit((LiteralNode) node, data);
  }

  default T visit(HexLiteralNode node, T data) {
    return visit((LiteralNode) node, data);
  }

  default T visit(IntegerLiteralNode node, T data) {
    return visit((LiteralNode) node, data);
  }

  default T visit(NilLiteralNode node, T data) {
    return visit((LiteralNode) node, data);
  }

  default T visit(TextLiteralNode node, T data) {
    return visit((LiteralNode) node, data);
  }

  /* Statements */
  default T visit(StatementNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AsmStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(AssignmentStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(CaseItemStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(CaseStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(CompoundStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(ConstStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(ExpressionStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(ForStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(ForToStatementNode node, T data) {
    return visit((ForStatementNode) node, data);
  }

  default T visit(ForInStatementNode node, T data) {
    return visit((ForStatementNode) node, data);
  }

  default T visit(GotoStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(IfStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(LabelStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(RaiseStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(RepeatStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(TryStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(VarStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(WhileStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }

  default T visit(WithStatementNode node, T data) {
    return visit((StatementNode) node, data);
  }
}
