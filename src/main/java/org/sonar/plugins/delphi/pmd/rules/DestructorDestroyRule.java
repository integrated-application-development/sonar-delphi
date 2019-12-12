package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;

public class DestructorDestroyRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodDeclarationNode method, RuleContext data) {
    if (isViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }
    return super.visit(method, data);
  }

  private boolean isViolation(MethodDeclarationNode method) {
    if (!method.isDestructor() || method.isClassMethod()) {
      return false;
    }

    return !(method.simpleName().equalsIgnoreCase("Destroy")
        && method.getParameters().isEmpty()
        && method.isOverride());
  }
}
