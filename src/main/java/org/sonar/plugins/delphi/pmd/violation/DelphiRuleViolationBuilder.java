package org.sonar.plugins.delphi.pmd.violation;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

public class DelphiRuleViolationBuilder {
  private final DelphiRule rule;
  private final RuleContext ctx;
  private final DelphiRuleViolation ruleViolation;

  private DelphiRuleViolationBuilder(DelphiRule rule, RuleContext ctx) {
    this.rule = rule;
    this.ctx = ctx;
    this.ruleViolation = new DelphiRuleViolation(rule, ctx);
  }

  public static DelphiRuleViolationBuilder newViolation(DelphiRule rule, RuleContext ctx) {
    return new DelphiRuleViolationBuilder(rule, ctx);
  }

  public DelphiRuleViolationBuilder atPosition(FilePosition position) {
    ruleViolation.setBeginLine(position.getBeginLine());
    ruleViolation.setBeginColumn(position.getBeginColumn());
    ruleViolation.setEndLine(position.getEndLine());
    ruleViolation.setEndColumn(position.getEndColumn());
    return this;
  }

  public DelphiRuleViolationBuilder atLocation(DelphiNode node) {
    findLogicalLocation(node);
    return this;
  }

  public DelphiRuleViolationBuilder message(String message) {
    ruleViolation.setDescription(message);
    return this;
  }

  public RuleViolation build() {
    checkIfViolationSuppressed();
    return ruleViolation;
  }

  public void save() {
    ctx.getReport().addRuleViolation(build());
  }

  private void checkIfViolationSuppressed() {
    boolean suppressed = rule.getSuppressions().contains(ruleViolation.getBeginLine());
    ruleViolation.setSuppressed(suppressed);
  }

  private void findLogicalLocation(DelphiNode node) {
    FileHeaderNode fileHeader = node.getASTTree().getFileHeader();
    ruleViolation.setPackageName(fileHeader.getName());

    TypeDeclarationNode typeNode = node.getFirstParentOfType(TypeDeclarationNode.class);
    if (typeNode != null) {
      ruleViolation.setClassName(typeNode.fullyQualifiedName());
    }

    MethodImplementationNode methodNode = node.getFirstParentOfType(MethodImplementationNode.class);
    if (methodNode != null) {
      ruleViolation.setClassName(methodNode.getTypeName());
      ruleViolation.setMethodName(methodNode.simpleName());
    }
  }
}
