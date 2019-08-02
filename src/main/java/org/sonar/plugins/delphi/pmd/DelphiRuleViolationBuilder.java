package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

public class DelphiRuleViolationBuilder {
  private static final int[] METHOD_TYPES = {
    DelphiLexer.CONSTRUCTOR, DelphiLexer.DESTRUCTOR, DelphiLexer.FUNCTION, DelphiLexer.PROCEDURE
  };

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
    Tree unitNode = findUnitNode(node);

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
      StringBuilder qualifiedName = new StringBuilder();
      Tree nameNode = ((CommonTree) methodNode).getFirstChildWithType(DelphiLexer.TkFunctionName);
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        qualifiedName.append(nameNode.getChild(i).getText());
      }

      String methodName = qualifiedName.toString();
      int dotIndex = methodName.lastIndexOf('.');

      if (dotIndex > 0) {
        ruleViolation.setClassName(methodName.substring(0, dotIndex));
        ruleViolation.setMethodName(methodName.substring(dotIndex + 1));
      } else {
        ruleViolation.setMethodName(methodName);
      }
    }
  }

  private Tree findUnitNode(DelphiPMDNode node) {
    return node.getASTTree().getFirstChildWithType(DelphiLexer.UNIT);
  }

  private Tree findMethodNode(DelphiPMDNode node) {
    Tree methodNode = null;

    for (int methodType : METHOD_TYPES) {
      methodNode = node.getAncestor(methodType);

      if (methodNode != null) {
        return methodNode;
      }
    }

    // look for method from begin...end statements
    Tree currentNode = node;

    while (methodNode == null) {
      currentNode = currentNode.getAncestor(DelphiLexer.BEGIN);
      if (currentNode == null) {
        break;
      }

      methodNode = findMethodNodeFromBeginNode(currentNode);
    }

    return methodNode;
  }

  private Tree findMethodNodeFromBeginNode(Tree beginNode) {
    int index = beginNode.getChildIndex();

    if (index > 1) {
      Tree parent = beginNode.getParent();
      Tree node = parent.getChild(index - 2);

      if (isMethodNode(node.getType())) {
        return node;
      }
    }

    return null;
  }

  private static boolean isMethodNode(int type) {
    for (int methodType : METHOD_TYPES) {
      if (methodType == type) {
        return true;
      }
    }
    return false;
  }
}
