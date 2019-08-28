package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class EnumNameRule extends AbstractDelphiRule {
  private static final String PREFIX = "T";

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isEnum() && !NameConventionUtils.compliesWithPrefix(type.simpleName(), PREFIX)) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
