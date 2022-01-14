package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;

public class UnusedPropertiesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(PropertyNode property, RuleContext data) {
    NameDeclarationNode name = property.getPropertyName();
    if (!property.isPublished() && name.getUsages().isEmpty()) {
      addViolation(data, name);
    }
    return data;
  }
}
