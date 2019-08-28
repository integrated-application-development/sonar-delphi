package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ClassNameRule extends AbstractDelphiRule {
  private static final String[] PREFIXES = {"T", "TForm", "TFrame", "E"};

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isClass() && !NameConventionUtils.compliesWithPrefix(type.simpleName(), PREFIXES)) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
