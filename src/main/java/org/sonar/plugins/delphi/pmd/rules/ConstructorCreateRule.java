package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class ConstructorCreateRule extends AbstractDelphiRule {
  private static final String PREFIX = "Create";

  @Override
  public RuleContext visit(MethodHeadingNode method, RuleContext data) {
    if (isViolation(method)) {
      addViolation(data, method);
    }
    return super.visit(method, data);
  }

  private boolean isViolation(MethodHeadingNode method) {
    if (method.isMethodImplementation() || !method.isConstructor()) {
      return false;
    }

    return !PREFIX.equals(method.getSimpleName())
        && !NameConventionUtils.compliesWithPrefix(method.getSimpleName(), PREFIX);
  }
}
