package org.sonar.plugins.delphi.pmd.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;

public class UnusedImportsRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> EXCLUSIONS_PROPERTY =
      PropertyFactory.stringListProperty("exclusions")
          .desc("Fully-qualified names of unit imports to exclude.")
          .emptyDefaultValue()
          .build();

  private final Map<UnitImportNode, UnitImportNameDeclaration> imports = new HashMap<>();
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
    super.visit(ast, data);
    imports.keySet().forEach(node -> this.addViolation(data, node));
    imports.clear();
    return data;
  }

  @Override
  public RuleContext visit(UnitImportNode unitImport, RuleContext data) {
    if (unitImport.isResolvedImport()) {
      UnitImportNameDeclaration importDeclaration = unitImport.getImportNameDeclaration();
      if (!isExcluded(importDeclaration)) {
        imports.put(unitImport, importDeclaration);
      }
    }
    return data;
  }

  private boolean isExcluded(UnitImportNameDeclaration declaration) {
    UnitNameDeclaration unitDeclaration = declaration.getOriginalDeclaration();
    return unitDeclaration == null || exclusions.contains(unitDeclaration.getImage());
  }

  @Override
  public RuleContext visit(NameReferenceNode nameNode, RuleContext data) {
    if (!imports.isEmpty() && isRelevantNameReferenceNode(nameNode)) {
      removeImportsTraversedByNameReference(nameNode);
    }
    return super.visit(nameNode, data);
  }

  private static boolean isRelevantNameReferenceNode(NameReferenceNode nameNode) {
    return isNameStart(nameNode) || isFromHelperType(nameNode);
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

  private void removeImportsTraversedByNameReference(NameReferenceNode nameNode) {
    DelphiNameDeclaration declaration = nameNode.getNameDeclaration();
    if (declaration != null) {
      // Remove imports that are explicitly referenced
      if (imports.entrySet().removeIf(entry -> declaration == entry.getValue())) {
        return;
      }

      // Remove imports that are implicitly referenced
      FileScope scope = declaration.getScope().getEnclosingScope(FileScope.class);
      if (scope != null) {
        imports.entrySet().removeIf(entry -> scope == entry.getValue().getUnitScope());
      }
    }
  }
}
