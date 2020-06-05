package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class EmptyMethodRule extends AbstractDelphiRule {

  private final List<MethodDeclarationNode> methodDeclarations = new ArrayList<>();

  @Override
  public void start(RuleContext ctx) {
    methodDeclarations.clear();
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (method.isEmptyMethod() && shouldAddViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }
    return super.visit(method, data);
  }

  @Override
  public RuleContext visit(MethodDeclarationNode node, RuleContext data) {
    methodDeclarations.add(node);
    return data;
  }

  private boolean shouldAddViolation(MethodImplementationNode method) {
    DelphiNode block = method.getBlock();

    if (block != null && block.getComments().isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    return methodDeclarations.stream()
        .filter(decl -> decl.isOverride() || decl.isVirtual())
        .noneMatch(decl -> decl.getImage().equals(method.getImage()));
  }
}
