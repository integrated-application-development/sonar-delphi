package org.sonar.plugins.delphi.pmd.rules;

import static java.lang.String.format;

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

  private static final String MESSAGE =
      "Extract this deeply nested method. Nesting level is %d. (Limit is %d)";

  public MethodNestingDepthRule() {
    definePropertyDescriptor(DEPTH);
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int depth = method.getParentsOfType(MethodImplementationNode.class).size();
    int limit = getProperty(DEPTH);

    if (depth > limit) {
      addViolationWithMessage(data, method.getMethodNameNode(), format(MESSAGE, depth, limit));
    }

    return super.visit(method, data);
  }
}
