package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class MethodNestingDepthRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<Integer> DEPTH =
      PropertyFactory.intProperty("depth")
          .desc("The maximum nesting level allowed for a nested method.")
          .defaultValue(1)
          .build();

  public MethodNestingDepthRule() {
    definePropertyDescriptor(DEPTH);
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int depth = method.getParentsOfType(MethodImplementationNode.class).size();
    int limit = getProperty(DEPTH);

    if (depth > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Extract this deeply nested method. Nesting level is {0}. (Limit is {1})",
          new Object[] {depth, limit});
    }

    return super.visit(method, data);
  }
}
