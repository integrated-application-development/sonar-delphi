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
package au.com.integradev.delphi.antlr.ast.visitors;

import au.com.integradev.delphi.antlr.ast.visitors.DependencyAnalysisVisitor.Data;
import au.com.integradev.delphi.symbol.declaration.RoutineNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclarationImpl;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.symbol.EnumeratorOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public abstract class DependencyAnalysisVisitor implements DelphiParserVisitor<Data> {
  private static final String TCOMPONENT = "System.Classes.TComponent";

  public static class Data {
    private final UnitNameDeclaration unitDeclaration;
    private RoutineNameDeclaration routine;
    private boolean implementation;

    public Data(UnitNameDeclaration unitDeclaration) {
      this.unitDeclaration = unitDeclaration;
    }

    private void addDependency(UnitNameDeclaration dependency) {
      if (routine != null) {
        ((RoutineNameDeclarationImpl) routine).addDependency(dependency);
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
  public Data visit(RoutineImplementationNode routineNode, Data data) {
    RoutineNameDeclaration previousRoutine = data.routine;
    data.routine = routineNode.getRoutineNameDeclaration();
    DelphiParserVisitor.super.visit(routineNode, data);
    data.routine = previousRoutine;
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

    // Weak alias references indicate a dependency on the aliased type declaration
    handleWeakAliases(declaration, data);

    // Inline routine dependencies should be included in the call site's dependencies
    // Inline routines cannot be expanded by the compiler unless these dependencies are present
    handleInlineRoutines(declaration, data);

    return DelphiParserVisitor.super.visit(nameNode, data);
  }

  private static void handleExplicitImportReferences(
      @Nullable NameDeclaration declaration, Data data) {
    if (declaration instanceof UnitImportNameDeclaration) {
      addDependenciesForDeclaration(
          ((UnitImportNameDeclaration) declaration).getOriginalDeclaration(), data);
    }
  }

  private static void handleWeakAliases(@Nullable NameDeclaration declaration, Data data) {
    if (declaration instanceof TypeNameDeclaration) {
      addDependenciesForDeclaration(((TypeNameDeclarationImpl) declaration).getAliased(), data);
    }
  }

  private static void handleInlineRoutines(@Nullable NameDeclaration declaration, Data data) {
    if (isInlineRoutineReference(declaration)) {
      addDependenciesRequiredByRoutine(declaration, data);
    }

    // Inline routines are also expanded via property references
    if (declaration instanceof PropertyNameDeclaration) {
      PropertyNameDeclaration property = (PropertyNameDeclaration) declaration;
      handleInlineRoutines(property.getReadDeclaration(), data);
      handleInlineRoutines(property.getWriteDeclaration(), data);
    }
  }

  @Override
  public Data visit(ArrayAccessorNode accessorNode, Data data) {
    NameOccurrence implicitOccurrence = accessorNode.getImplicitNameOccurrence();
    if (implicitOccurrence != null) {
      handleInlineRoutines(implicitOccurrence.getNameDeclaration(), data);
    }
    return DelphiParserVisitor.super.visit(accessorNode, data);
  }

  @Override
  public Data visit(ForInStatementNode forInStatementNode, Data data) {
    EnumeratorOccurrence enumerator = forInStatementNode.getEnumeratorOccurrence();

    if (enumerator != null) {
      var getEnumerator =
          (RoutineNameDeclaration) enumerator.getGetEnumerator().getNameDeclaration();
      addDependenciesForDeclaration(getEnumerator, data);
      handleInlineRoutines(getEnumerator, data);

      var moveNext = (RoutineNameDeclaration) enumerator.getMoveNext().getNameDeclaration();
      addDependenciesForDeclaration(moveNext, data);
      handleInlineRoutines(moveNext, data);

      var current = (PropertyNameDeclaration) enumerator.getCurrent().getNameDeclaration();
      addDependenciesForDeclaration(current, data);
      handleInlineRoutines(current, data);
    }

    return DelphiParserVisitor.super.visit(forInStatementNode, data);
  }

  private static void addDependenciesForDeclaration(
      @Nullable NameDeclaration declaration, Data data) {
    if (declaration != null) {
      FileScope scope = declaration.getScope().getEnclosingScope(FileScope.class);
      addDependencyByFileScope(scope, data);
    }
  }

  private static void addDependenciesForComponentTypeDeclaration(Type type, Data data) {
    if (isComponent(type)) {
      ((ScopedType) type)
          .typeScope()
          .getVariableDeclarations()
          .forEach(declaration -> addDependenciesForComponentTypeField(declaration, data));

      addDependenciesForComponentTypeDeclaration(type.parent(), data);
    }
  }

  private static void addDependenciesRequiredByRoutine(NameDeclaration declaration, Data data) {
    ((RoutineNameDeclarationImpl) declaration).getDependencies().forEach(data::addDependency);
    addDependenciesForDeclaration(declaration, data);
  }

  private static void addDependenciesForComponentTypeField(
      VariableNameDeclaration field, Data data) {
    Type type = field.getType();
    if (field.isPublished() && isComponent(type)) {
      while (addDependenciesDeclaringType(type, data)) {
        type = type.parent();
      }
    }
  }

  private static boolean addDependenciesDeclaringType(Type type, Data data) {
    FileScope fileScope = getFileScopeFromType(type);
    if (fileScope != null) {
      addDependencyByFileScope(fileScope, data);
      return true;
    }
    return false;
  }

  private static void addDependencyByFileScope(FileScope fileScope, Data data) {
    if (fileScope != data.unitDeclaration.getFileScope()) {
      data.addDependency(fileScope.getUnitDeclaration());
    }
  }

  private static boolean isComponent(Type type) {
    return type.isDescendantOf(TCOMPONENT) || type.is(TCOMPONENT);
  }

  private static boolean isNameStart(NameReferenceNode nameNode) {
    DelphiNode parent = nameNode.getParent();
    if (parent instanceof NameReferenceNode) {
      return false;
    }

    if (parent instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primaryExpression = (PrimaryExpressionNode) parent;
      int nameStartIndex = ExpressionNodeUtils.isInherited(primaryExpression) ? 1 : 0;
      return nameNode.getChildIndex() == nameStartIndex;
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

  private static boolean isInlineRoutineReference(NameDeclaration declaration) {
    if (declaration instanceof RoutineNameDeclaration) {
      return ((RoutineNameDeclaration) declaration).hasDirective(RoutineDirective.INLINE);
    }
    return false;
  }
}
