package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class EmptyMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (method.isEmptyMethod() && shouldAddViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }
    return super.visit(method, data);
  }

  private boolean shouldAddViolation(MethodImplementationNode method) {
    DelphiNode block = method.getBlock();

    if (block != null && block.getComments().isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration == null) {
      return true;
    }

    return !declaration.hasDirective(MethodDirective.OVERRIDE)
        && !declaration.hasDirective(MethodDirective.VIRTUAL)
        && !declaration.implementsInterface();
  }
}
