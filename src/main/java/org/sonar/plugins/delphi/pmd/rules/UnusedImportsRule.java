package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Typed;

public class UnusedImportsRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> EXCLUSIONS_PROPERTY =
      PropertyFactory.stringListProperty("exclusions")
          .desc("Fully-qualified names of unit imports to exclude (only in the interface section)")
          .emptyDefaultValue()
          .build();

  private static final String TCOMPONENT = "System.Classes.TComponent";

  private final List<UnitImportNode> imports = new ArrayList<>();
  private final Set<String> exclusions = new HashSet<>();

  public UnusedImportsRule() {
    definePropertyDescriptor(EXCLUSIONS_PROPERTY);
  }

  @Override
  public void start(RuleContext ctx) {
    exclusions.clear();
    exclusions.addAll(getProperty(EXCLUSIONS_PROPERTY));
  }

  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    if (ast.isUnit()) {
      super.visit(ast, data);
      imports.forEach(node -> this.addViolation(data, node));
      imports.clear();
    }
    return data;
  }

  @Override
  public RuleContext visit(UnitImportNode unitImport, RuleContext data) {
    if (!isExcluded(unitImport)) {
      imports.add(unitImport);
    }
    return data;
  }

  private boolean isExcluded(UnitImportNode unitImport) {
    if (!unitImport.isResolvedImport()) {
      return true;
    }

    if (unitImport.isInterfaceSection()) {
      UnitImportNameDeclaration importDeclaration = unitImport.getImportNameDeclaration();
      UnitNameDeclaration unitDeclaration = importDeclaration.getOriginalDeclaration();
      Objects.requireNonNull(unitDeclaration);
      return exclusions.contains(unitDeclaration.getImage());
    }

    return false;
  }

  @Override
  public RuleContext visit(NameReferenceNode nameNode, RuleContext data) {
    if (!imports.isEmpty()) {
      if (isNameStart(nameNode) || isFromHelperType(nameNode)) {
        removeImportsTraversedByNameReference(nameNode);
      }

      removeImportsRequiredByInlineMethod(nameNode);
    }
    return super.visit(nameNode, data);
  }

  @Override
  public RuleContext visit(TypeDeclarationNode typeDeclaration, RuleContext data) {
    if (typeDeclaration.isInterfaceSection()) {
      removeImportsForComponentTypeDeclaration(typeDeclaration.getType());
    }
    return super.visit(typeDeclaration, data);
  }

  private void removeImportsTraversedByNameReference(NameReferenceNode nameNode) {
    DelphiNameDeclaration declaration = nameNode.getNameDeclaration();
    if (declaration != null) {
      // Remove imports that are explicitly referenced
      if (removeImportIf(unitImport -> declaration == unitImport)) {
        return;
      }

      // Remove imports that are implicitly referenced
      FileScope scope = declaration.getScope().getEnclosingScope(FileScope.class);
      if (scope != null) {
        removeImportIf(unitImport -> scope == unitImport.getUnitScope());
      }
    }
  }

  private void removeImportsForComponentTypeDeclaration(Type type) {
    if (!imports.isEmpty() && isComponent(type)) {
      ((ScopedType) type)
          .typeScope()
          .getVariableDeclarations()
          .forEach(this::removeImportsForComponentTypeField);

      removeImportsForComponentTypeDeclaration(type.superType());
    }
  }

  private void removeImportsRequiredByInlineMethod(NameReferenceNode nameNode) {
    if (!imports.isEmpty() && isInlineMethodReference(nameNode)) {
      MethodNameDeclaration method = (MethodNameDeclaration) nameNode.getNameDeclaration();
      removeImportsTraversedByNameReference(nameNode);
      removeImportsDeclaringType(method.getReturnType());
      method.getParameters().stream().map(Typed::getType).forEach(this::removeImportsDeclaringType);
    }
  }

  private void removeImportsForComponentTypeField(VariableNameDeclaration field) {
    Type type = field.getType();
    if (field.isPublished() && isComponent(type)) {
      while (removeImportsDeclaringType(type)) {
        type = type.superType();
      }
    }
  }

  private boolean removeImportsDeclaringType(Type type) {
    FileScope fileScope = getFileScopeFromType(type);
    if (fileScope == null) {
      return false;
    }
    removeImportIf(unitImport -> fileScope == unitImport.getUnitScope());
    return true;
  }

  private static boolean isComponent(Type type) {
    return type.isSubTypeOf(TCOMPONENT) || type.is(TCOMPONENT);
  }

  private static boolean isNameStart(NameReferenceNode nameNode) {
    return !(nameNode.jjtGetParent() instanceof NameReferenceNode)
        && !(nameNode.jjtGetParent() instanceof PrimaryExpressionNode
            && nameNode.jjtGetChildIndex() > 0);
  }

  private static boolean isFromHelperType(NameReferenceNode nameNode) {
    DelphiNameDeclaration declaration = nameNode.getNameDeclaration();
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

  private static boolean isInlineMethodReference(NameReferenceNode nameNode) {
    NameDeclaration declaration = nameNode.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      return ((MethodNameDeclaration) declaration).getDirectives().contains(MethodDirective.INLINE);
    }
    return false;
  }

  private boolean removeImportIf(Predicate<UnitImportNameDeclaration> filter) {
    return imports.removeIf(node -> filter.test(node.getImportNameDeclaration()));
  }
}
