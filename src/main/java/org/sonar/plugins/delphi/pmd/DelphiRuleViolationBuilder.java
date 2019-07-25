package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

public class DelphiRuleViolationBuilder {
  private DelphiRuleViolation ruleViolation;
  private RuleContext ctx;

  private DelphiRuleViolationBuilder(DelphiRule rule, RuleContext ctx) {
    this.ruleViolation = new DelphiRuleViolation(rule, ctx);
    this.ctx = ctx;
  }

  public static DelphiRuleViolationBuilder newViolation(DelphiRule rule, RuleContext ctx) {
    return new DelphiRuleViolationBuilder(rule, ctx);
  }

  public DelphiRuleViolationBuilder fileLocation(int line, int column, int endLine, int endColumn) {
    ruleViolation.setBeginLine(line);
    ruleViolation.setBeginColumn(column);
    ruleViolation.setEndLine(endLine);
    ruleViolation.setEndColumn(endColumn);
    return this;
  }

  public DelphiRuleViolationBuilder fileLocation(DelphiPMDNode node) {
    return fileLocation(
        node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn());
  }

  public DelphiRuleViolationBuilder logicalLocation(
      String packageName, String className, String methodName) {
    ruleViolation.setPackageName(packageName);
    ruleViolation.setClassName(className);
    ruleViolation.setMethodName(methodName);
    return this;
  }

  public DelphiRuleViolationBuilder logicalLocation(DelphiPMDNode node) {
    findLogicalLocation(node);
    return this;
  }

  public DelphiRuleViolationBuilder message(String message) {
    ruleViolation.setDescription(message);
    return this;
  }

  public void save() {
    ctx.getReport().addRuleViolation(ruleViolation);
  }

  private void findLogicalLocation(DelphiPMDNode node) {
    Tree unitNode = node.getAncestor(DelphiLexer.UNIT);

    if (unitNode != null) {
      StringBuilder name = new StringBuilder();
      for (int i = 0; i < unitNode.getChildCount(); ++i) {
        name.append(unitNode.getChild(i).getText());
      }

      ruleViolation.setPackageName(name.toString());
    }

    Tree classTypeNode = node.getAncestor(DelphiLexer.TkNewType);
    if (classTypeNode != null) {
      Tree classNameNode = classTypeNode.getChild(0);
      ruleViolation.setClassName(classNameNode.getText());
    }

    Tree methodNode = findMethodNode(node);

    if (methodNode != null) {
      StringBuilder name = new StringBuilder();
      Tree nameNode = ((CommonTree) methodNode).getFirstChildWithType(DelphiLexer.TkFunctionName);
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        name.append(nameNode.getChild(i).getText());
      }

      ruleViolation.setMethodName(name.toString());

      if (nameNode.getChildCount() > 1) {
        // class name from function name
        ruleViolation.setClassName(nameNode.getChild(0).getText());
      }
    }
  }

  private Tree findMethodNode(DelphiPMDNode node) {
    Tree methodNode = node.getAncestor(DelphiLexer.FUNCTION);

    if (methodNode != null) {
      return methodNode;
    }

    methodNode = node.getAncestor(DelphiLexer.PROCEDURE);

    if (methodNode != null) {
      return methodNode;
    }

    // look for method from begin...end statements
    Tree currentNode = node;
    Tree beginNode;

    while (methodNode == null) {
      beginNode = currentNode.getAncestor(DelphiLexer.BEGIN);
      if (beginNode == null) {
        break;
      }

      currentNode = beginNode.getParent();
      methodNode = findMethodNodeFromBeginNode(beginNode);
    }

    return methodNode;
  }

  private Tree findMethodNodeFromBeginNode(Tree beginNode) {
    int index = beginNode.getChildIndex();
    Tree parent = beginNode.getParent();
    Tree possibleMethodNode;
    Tree methodNode = null;

    for (int lookBack = 1; lookBack <= 2; ++lookBack) {
      if (index - lookBack > -1) {
        possibleMethodNode = parent.getChild(index - lookBack);

        if (isProcedureOrFunction(possibleMethodNode.getType())) {
          methodNode = possibleMethodNode;
          break;
        }
      }
    }

    return methodNode;
  }

  private boolean isProcedureOrFunction(int type) {
    return type == DelphiLexer.PROCEDURE || type == DelphiLexer.FUNCTION;
  }
}
