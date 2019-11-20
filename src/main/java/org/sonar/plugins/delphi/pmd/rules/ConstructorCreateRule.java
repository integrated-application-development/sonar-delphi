package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ConstructorCreateRule extends AbstractDelphiRule {
  private static final String PREFIX = "Create";

  @Override
  public RuleContext visit(MethodDeclarationNode method, RuleContext data) {
    if (isViolation(method)) {
      addViolation(data, method.getMethodName());
    }
    return super.visit(method, data);
  }

  private boolean isViolation(MethodDeclarationNode method) {
    if (!method.isConstructor() || method.isClassMethod()) {
      return false;
    }

    return !PREFIX.equals(method.simpleName())
        && !NameConventionUtils.compliesWithPrefix(method.simpleName(), PREFIX);
  }
}
