package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;

/** Rule type which only applies to dpr and dpk files */
public abstract class DprRule extends DelphiRule {
  @Override
  protected void visitAll(List<? extends Node> acus, RuleContext ctx) {
    String fileName = ctx.getSourceCodeFile().getName().toLowerCase();
    if (!fileName.endsWith(".dpr")) {
      return;
    }

    super.visitAll(acus, ctx);
  }
}
