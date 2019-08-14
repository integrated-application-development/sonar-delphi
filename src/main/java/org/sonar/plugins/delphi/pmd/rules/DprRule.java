package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;

/** Rule type which only applies to dpr and dpk files */
public abstract class DprRule extends DelphiRule {
  @Override
  public void apply(List<? extends Node> nodes, RuleContext ctx) {
    String fileName = ctx.getSourceCodeFile().getName().toLowerCase();
    if (!fileName.endsWith(".dpr")) {
      return;
    }

    super.apply(nodes, ctx);
  }
}
