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
package au.com.integradev.delphi.antlr.ast.visitors;

import org.sonar.plugins.communitydelphi.api.ast.AncestorListNode;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayIndicesNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeGroupNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassReferenceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstArraySubTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ContainsClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.DecimalLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.EnumElementNode;
import org.sonar.plugins.communitydelphi.api.ast.EnumTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.FileTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.FinallyBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericArgumentsNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ImportClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceGuidNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.LabelStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.LibraryDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.LiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodResolutionClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.ObjectTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.PackageDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PointerTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ProceduralTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureReferenceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProgramDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyReadSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyWriteSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RangeExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordExpressionItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantTagNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RequiresClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.SetTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.StringTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.SubRangeTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeAliasNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeOfTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.ast.UsesClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WithStatementNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public interface DelphiParserVisitor<T> {

  default T visit(DelphiNode node, T data) {
    return node.childrenAccept(this, data);
  }

  default T visit(CommonDelphiNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(DelphiAst node, T data) {
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

  default T visit(AttributeNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AttributeGroupNode node, T data) {
    return visit((DelphiNode) node, data);
  }

  default T visit(AttributeListNode node, T data) {
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
