package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ConstantNotationRule extends AbstractDelphiRule {

  private static final String PREFIX = "C_";

  @Override
  public RuleContext visit(ConstDeclarationNode declaration, RuleContext data) {
    if (!NameConventionUtils.compliesWithPrefix(
        declaration.getNameDeclarationNode().getImage(), PREFIX)) {
      addViolation(data, declaration.getNameDeclarationNode());
    }
    return super.visit(declaration, data);
  }
}
