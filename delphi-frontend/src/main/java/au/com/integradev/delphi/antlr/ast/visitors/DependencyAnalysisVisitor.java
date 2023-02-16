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
package au.com.integradev.delphi.antlr.ast.visitors;

import au.com.integradev.delphi.antlr.ast.visitors.DependencyAnalysisVisitor.Data;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclarationImpl;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;

public abstract class DependencyAnalysisVisitor implements DelphiParserVisitor<Data> {
  private static final String TCOMPONENT = "System.Classes.TComponent";

  public static class Data {
    private final UnitNameDeclaration unitDeclaration;
    private MethodNameDeclaration method;
    private boolean implementation;

    public Data(UnitNameDeclaration unitDeclaration) {
      this.unitDeclaration = unitDeclaration;
    }

    private void addDependency(UnitNameDeclaration dependency) {
      if (method != null) {
        ((MethodNameDeclarationImpl) method).addDependency(dependency);
      }

      if (implementation) {
        ((UnitNameDeclarationImpl) unitDeclaration).addImplementationDependency(dependency);
      } else {
        ((UnitNameDeclarationImpl) unitDeclaration).addInterfaceDependency(dependency);
      }
    }
  }

  public static DependencyAnalysisVisitor interfaceVisitor() {
    return new DependencyAnalysisVisitor() {
      @Override
      public Data visit(ImplementationSectionNode node, Data data) {
        return data;
      }

      @Override
      public Data visit(InitializationSectionNode node, Data data) {
        return data;
      }

      @Override
      public Data visit(FinalizationSectionNode node, Data data) {
        return data;
      }
    };
  }

  public static DependencyAnalysisVisitor implementationVisitor() {
    return new DependencyAnalysisVisitor() {
      @Override
      public Data visit(InterfaceSectionNode node, Data data) {
        return data;
      }
    };
  }

  @Override
  public Data visit(TypeDeclarationNode typeDeclaration, Data data) {
    if (typeDeclaration.getFirstParentOfType(InterfaceSectionNode.class) != null) {
      addDependenciesForComponentTypeDeclaration(typeDeclaration.getType(), data);
    }
    return DelphiParserVisitor.super.visit(typeDeclaration, data);
  }

  @Override
  public Data visit(ImplementationSectionNode implementationSection, Data data) {
    data.implementation = true;
    return DelphiParserVisitor.super.visit(implementationSection, data);
  }

  @Override
  public Data visit(MethodImplementationNode methodNode, Data data) {
    MethodNameDeclaration previousMethod = data.method;
    data.method = methodNode.getMethodNameDeclaration();
    DelphiParserVisitor.super.visit(methodNode, data);
    data.method = previousMethod;
    return data;
  }

  @Override
  public Data visit(NameReferenceNode nameNode, Data data) {
    NameDeclaration declaration = nameNode.getNameDeclaration();

    if (isNameStart(nameNode) || isFromHelperType(declaration)) {
      addDependenciesForDeclaration(declaration, data);
    }

    // Explicit references to an import indicates a dependency on the unit being imported
    handleExplicitImportReferences(declaration, data);

    // Type alias references indicate a dependency on the aliased type declaration
    handleTypeAliases(declaration, data);

    // Inline method dependencies should be included in the call site's dependencies
    // Inline methods cannot be expanded by the compiler unless these dependencies are present
    handleInlineMethods(declaration, data);

    return DelphiParserVisitor.super.visit(nameNode, data);
  }

  private void handleExplicitImportReferences(@Nullable NameDeclaration declaration, Data data) {
    if (declaration instanceof UnitImportNameDeclaration) {
      addDependenciesForDeclaration(
          ((UnitImportNameDeclaration) declaration).getOriginalDeclaration(), data);
    }
  }

  private void handleTypeAliases(@Nullable NameDeclaration declaration, Data data) {
    if (declaration instanceof TypeNameDeclaration) {
      addDependenciesForDeclaration(((TypeNameDeclaration) declaration).getAliased(), data);
    }
  }

  private void handleInlineMethods(@Nullable NameDeclaration declaration, Data data) {
    if (isInlineMethodReference(declaration)) {
      addDependenciesRequiredByMethod(declaration, data);
    }

    // Inline methods are also expanded via property references
    if (declaration instanceof PropertyNameDeclaration) {
      PropertyNameDeclaration property = (PropertyNameDeclaration) declaration;
      handleInlineMethods(property.getReadDeclaration(), data);
      handleInlineMethods(property.getWriteDeclaration(), data);
    }
  }

  @Override
  public Data visit(ArrayAccessorNode accessorNode, Data data) {
    NameOccurrence implicitOccurrence = accessorNode.getImplicitNameOccurrence();
    if (implicitOccurrence != null) {
      handleInlineMethods(implicitOccurrence.getNameDeclaration(), data);
    }
    return DelphiParserVisitor.super.visit(accessorNode, data);
  }

  @Override
  public Data visit(ForInStatementNode forInStatementNode, Data data) {
    MethodNameDeclaration enumerator = forInStatementNode.getGetEnumeratorDeclaration();
    addDependenciesForDeclaration(enumerator, data);
    handleInlineMethods(enumerator, data);

    MethodNameDeclaration moveNext = forInStatementNode.getMoveNextDeclaration();
    addDependenciesForDeclaration(moveNext, data);
    handleInlineMethods(moveNext, data);

    PropertyNameDeclaration current = forInStatementNode.getCurrentDeclaration();
    addDependenciesForDeclaration(current, data);
    handleInlineMethods(current, data);

    return DelphiParserVisitor.super.visit(forInStatementNode, data);
  }

  private void addDependenciesForDeclaration(@Nullable NameDeclaration declaration, Data data) {
    if (declaration != null) {
      FileScope scope = declaration.getScope().getEnclosingScope(FileScope.class);
      if (scope != null) {
        data.addDependency(scope.getUnitDeclaration());
      }
    }
  }

  private void addDependenciesForComponentTypeDeclaration(Type type, Data data) {
    if (isComponent(type)) {
      ((ScopedType) type)
          .typeScope()
          .getVariableDeclarations()
          .forEach(declaration -> addDependenciesForComponentTypeField(declaration, data));

      addDependenciesForComponentTypeDeclaration(type.superType(), data);
    }
  }

  private void addDependenciesRequiredByMethod(NameDeclaration declaration, Data data) {
    ((MethodNameDeclarationImpl) declaration).getDependencies().forEach(data::addDependency);
    addDependenciesForDeclaration(declaration, data);
  }

  private void addDependenciesForComponentTypeField(VariableNameDeclaration field, Data data) {
    Type type = field.getType();
    if (field.isPublished() && isComponent(type)) {
      while (addDependenciesDeclaringType(type, data)) {
        type = type.superType();
      }
    }
  }

  private boolean addDependenciesDeclaringType(Type type, Data data) {
    FileScope fileScope = getFileScopeFromType(type);
    if (fileScope != null) {
      data.addDependency(fileScope.getUnitDeclaration());
      return true;
    }
    return false;
  }

  private static boolean isComponent(Type type) {
    return type.isSubTypeOf(TCOMPONENT) || type.is(TCOMPONENT);
  }

  private static boolean isNameStart(NameReferenceNode nameNode) {
    Node parent = nameNode.jjtGetParent();
    if (parent instanceof NameReferenceNode) {
      return false;
    }

    if (parent instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primaryExpression = (PrimaryExpressionNode) parent;
      int nameStartIndex = primaryExpression.isInheritedCall() ? 1 : 0;
      return nameNode.jjtGetChildIndex() == nameStartIndex;
    }

    return true;
  }

  private static boolean isFromHelperType(NameDeclaration declaration) {
    if (declaration == null) {
      return false;
    }

    TypeScope scope = declaration.getScope().getEnclosingScope(TypeScope.class);
    if (scope == null) {
      return false;
    }

    return scope.getType().isHelper();
  }

  @Nullable
  private static FileScope getFileScopeFromType(Type type) {
    if (type instanceof ScopedType) {
      return ((ScopedType) type).typeScope().getEnclosingScope(FileScope.class);
    }
    return null;
  }

  private static boolean isInlineMethodReference(NameDeclaration declaration) {
    if (declaration instanceof MethodNameDeclaration) {
      return ((MethodNameDeclaration) declaration).hasDirective(MethodDirective.INLINE);
    }
    return false;
  }
}
