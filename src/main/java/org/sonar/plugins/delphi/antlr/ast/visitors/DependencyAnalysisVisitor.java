package org.sonar.plugins.delphi.antlr.ast.visitors;

import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DependencyAnalysisVisitor.Data;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public abstract class DependencyAnalysisVisitor implements DelphiParserVisitor<Data> {
  private static final String TCOMPONENT = "System.Classes.TComponent";

  public static class Data {
    private UnitNameDeclaration unitDeclaration;
    private MethodNameDeclaration method;
    private boolean implementation;

    public Data(UnitNameDeclaration unitDeclaration) {
      this.unitDeclaration = unitDeclaration;
    }

    private void addDependency(UnitNameDeclaration dependency) {
      if (method != null) {
        method.addDependency(dependency);
      }

      if (implementation) {
        unitDeclaration.addImplementationDependency(dependency);
      } else {
        unitDeclaration.addInterfaceDependency(dependency);
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
    if (typeDeclaration.isInterfaceSection()) {
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
    DelphiNameDeclaration declaration = nameNode.getNameDeclaration();

    if (isNameStart(nameNode) || isFromHelperType(declaration)) {
      addDependenciesForDeclaration(declaration, data);
    }

    // Explicitly referenced dependencies
    if (declaration instanceof UnitImportNameDeclaration) {
      addDependenciesForDeclaration(
          ((UnitImportNameDeclaration) declaration).getOriginalDeclaration(), data);
    }

    // Type alias references indicate a dependency on the aliased type declaration
    if (declaration instanceof TypeNameDeclaration) {
      addDependenciesForDeclaration(((TypeNameDeclaration) declaration).getAliased(), data);
    }

    // Inline method dependencies should be included in the callsite's dependencies
    // Inline methods cannot be expanded by the compiler unless these dependencies are present
    if (isInlineMethodReference(declaration)) {
      addDependenciesRequiredByMethod(declaration, data);
    }

    return DelphiParserVisitor.super.visit(nameNode, data);
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
    ((MethodNameDeclaration) declaration).getDependencies().forEach(data::addDependency);
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
    return !(nameNode.jjtGetParent() instanceof NameReferenceNode)
        && !(nameNode.jjtGetParent() instanceof PrimaryExpressionNode
            && nameNode.jjtGetChildIndex() > 0);
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
