package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;

public class UnusedFieldsRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(FieldDeclarationNode field, RuleContext data) {
    if (!field.isPublished()) {
      field.getDeclarationList().getDeclarations().stream()
          .filter(node -> node.getUsages().isEmpty())
          .forEach(node -> addViolation(data, node));
    }
    return data;
  }
}
