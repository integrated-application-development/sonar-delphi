package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;

public class UnusedPropertiesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(PropertyNode property, RuleContext data) {
    NameDeclarationNode name = property.getPropertyName();
    PropertyNameDeclaration declaration = (PropertyNameDeclaration) name.getNameDeclaration();
    if (isUnused(declaration)) {
      addViolation(data, name);
    }
    return data;
  }

  private static boolean isUnused(PropertyNameDeclaration declaration) {
    return !declaration.isPublished()
        && declaration.getScope().getOccurrencesFor(declaration).isEmpty()
        && declaration.getRedeclarations().stream().allMatch(UnusedPropertiesRule::isUnused);
  }
}
